package com.mxxssm.modules.role;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.domain.Statuses;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.web.Pageables;
import com.mxxssm.modules.permission.PermissionResponse;
import com.mxxssm.modules.permission.PermissionService;
import com.mxxssm.modules.permission.SysPermission;
import com.mxxssm.modules.permission.SysPermissionRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final PermissionService permissionService;

    public RoleService(SysRoleRepository roleRepository, SysPermissionRepository permissionRepository,
            PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public PageResult<RoleResponse> page(String keyword, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Specification<SysRole> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(roleRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> active() {
        return roleRepository.findByStatusOrderByIdAsc(Statuses.ACTIVE).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> all() {
        return roleRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        String code = request.code().trim().toUpperCase();
        if (roleRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS, code);
        }
        SysRole entity = new SysRole();
        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setStatus(Statuses.isValid(request.status()) ? request.status() : Statuses.ACTIVE);
        return toResponse(roleRepository.save(entity));
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        SysRole entity = findById(id);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        if (Statuses.isValid(request.status())) {
            if (SysRole.BUILT_IN_CODES.contains(entity.getCode())
                    && Statuses.DISABLED.equals(request.status())) {
                throw new BusinessException(ErrorCode.ROLE_BUILT_IN);
            }
            entity.setStatus(request.status());
        }
        return toResponse(roleRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        SysRole entity = findById(id);
        if (SysRole.BUILT_IN_CODES.contains(entity.getCode())) {
            throw new BusinessException(ErrorCode.ROLE_BUILT_IN);
        }
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.ROLE_IN_USE, userCount);
        }
        entity.getPermissions().clear();
        roleRepository.save(entity);
        roleRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> permissions(Long roleId) {
        SysRole role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        return role.getPermissions().stream().map(permissionService::toResponse).toList();
    }

    @Transactional
    public void updatePermissions(Long roleId, Set<Long> permissionIds) {
        SysRole role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
        Set<Long> requested = permissionIds == null ? Set.of() : new HashSet<>(permissionIds);
        requested.remove(null);
        List<SysPermission> permissions = permissionRepository.findAllById(requested);
        if (permissions.size() != requested.size()) {
            throw new BusinessException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public SysRole findById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SysRole findByCode(String code) {
        return roleRepository.findByCode(code).orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
    }

    public RoleResponse toResponse(SysRole entity) {
        return new RoleResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                roleRepository.countUsersByRoleId(entity.getId()),
                entity.getCreatedAt());
    }
}
