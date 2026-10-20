package com.mxxssm.modules.user;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.domain.Statuses;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.security.SecurityUtils;
import com.mxxssm.common.web.Pageables;
import com.mxxssm.modules.auth.PasswordPolicy;
import com.mxxssm.modules.role.SysRole;
import com.mxxssm.modules.role.SysRoleRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    public UserService(SysUserRepository userRepository, SysRoleRepository roleRepository,
            PasswordEncoder passwordEncoder, PasswordPolicy passwordPolicy) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional(readOnly = true)
    public PageResult<UserResponse> page(String keyword, String status, String roleCode, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Specification<SysUser> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("username")), pattern),
                        builder.like(builder.lower(root.get("displayName")), pattern),
                        builder.like(builder.lower(root.get("email")), pattern)));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(roleCode)) {
                Join<SysUser, SysRole> roles = root.join("roles", JoinType.INNER);
                predicates.add(builder.equal(roles.get("code"), roleCode));
                query.distinct(true);
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(userRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, username);
        }
        passwordPolicy.validate(request.password());
        SysUser entity = new SysUser();
        entity.setUsername(username);
        entity.setDisplayName(request.displayName().trim());
        entity.setPassword(passwordEncoder.encode(request.password()));
        entity.setEmail(blankToNull(request.email()));
        entity.setPhone(blankToNull(request.phone()));
        entity.setStatus(Statuses.isValid(request.status()) ? request.status() : Statuses.ACTIVE);
        entity.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(entity));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        SysUser entity = userRepository.findWithAuthoritiesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long currentUserId = SecurityUtils.currentUserId();
        Set<SysRole> newRoles = resolveRoles(request.roles());
        String newStatus = Statuses.isValid(request.status()) ? request.status() : entity.getStatus();

        boolean self = id.equals(currentUserId);
        boolean removingOwnAdmin = self && !hasRole(newRoles, SysRole.ADMIN);
        if (removingOwnAdmin) {
            throw new BusinessException(ErrorCode.LAST_ADMIN);
        }
        if (self && Statuses.DISABLED.equals(newStatus)) {
            throw new BusinessException(ErrorCode.SELF_OPERATION);
        }
        protectLastAdmin(entity, newStatus, newRoles);

        entity.setDisplayName(request.displayName().trim());
        entity.setEmail(blankToNull(request.email()));
        entity.setPhone(blankToNull(request.phone()));
        entity.setStatus(newStatus);
        entity.getRoles().clear();
        entity.getRoles().addAll(newRoles);
        return toResponse(userRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        SysUser entity = userRepository.findWithAuthoritiesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (id.equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(ErrorCode.SELF_OPERATION);
        }
        if (isActiveAdmin(entity) && activeAdminCount() <= 1) {
            throw new BusinessException(ErrorCode.LAST_ADMIN);
        }
        entity.getRoles().clear();
        userRepository.save(entity);
        userRepository.delete(entity);
    }

    @Transactional
    public void updateLastLogin(Long id) {
        userRepository.updateLastLoginAt(id, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long activeAdminCount() {
        return userRepository.countByRoleCodeAndStatus(SysRole.ADMIN, Statuses.ACTIVE);
    }

    @Transactional(readOnly = true)
    public SysUser findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponse toResponse(SysUser entity) {
        List<String> roles = entity.getRoles().stream()
                .sorted(Comparator.comparing(SysRole::getId))
                .map(SysRole::getCode)
                .toList();
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus(),
                roles,
                entity.getLastLoginAt(),
                entity.getCreatedAt());
    }

    private Set<SysRole> resolveRoles(Set<String> roleCodes) {
        Set<SysRole> roles = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            if (!StringUtils.hasText(roleCode)) {
                continue;
            }
            SysRole role = roleRepository.findByCode(roleCode.trim().toUpperCase())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
            if (!Statuses.isActive(role.getStatus())) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
            }
            roles.add(role);
        }
        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return roles;
    }

    private void protectLastAdmin(SysUser entity, String newStatus, Set<SysRole> newRoles) {
        boolean wasActiveAdmin = isActiveAdmin(entity);
        boolean remainsActiveAdmin = Statuses.ACTIVE.equals(newStatus) && hasRole(newRoles, SysRole.ADMIN);
        if (wasActiveAdmin && !remainsActiveAdmin && activeAdminCount() <= 1) {
            throw new BusinessException(ErrorCode.LAST_ADMIN);
        }
    }

    private boolean isActiveAdmin(SysUser entity) {
        return Statuses.ACTIVE.equals(entity.getStatus()) && hasRole(entity.getRoles(), SysRole.ADMIN);
    }

    private boolean hasRole(Set<SysRole> roles, String code) {
        return roles.stream().anyMatch(role -> code.equals(role.getCode()));
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
