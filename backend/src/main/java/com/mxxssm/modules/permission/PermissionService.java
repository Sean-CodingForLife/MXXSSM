package com.mxxssm.modules.permission;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.domain.Statuses;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.web.Pageables;
import com.mxxssm.modules.menu.SysMenuRepository;
import com.mxxssm.modules.role.SysRoleRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {

    private final SysPermissionRepository permissionRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;

    public PermissionService(SysPermissionRepository permissionRepository, SysRoleRepository roleRepository,
            SysMenuRepository menuRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<PermissionResponse> page(String keyword, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Specification<SysPermission> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("name")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(permissionRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> all() {
        return permissionRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> active() {
        return permissionRepository.findByStatusOrderByIdAsc(Statuses.ACTIVE).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        String code = request.code().trim();
        if (permissionRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.PERMISSION_CODE_EXISTS, code);
        }
        SysPermission entity = new SysPermission();
        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setStatus(statusOrDefault(request.status()));
        return toResponse(permissionRepository.save(entity));
    }

    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {
        SysPermission entity = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_NOT_FOUND));
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setStatus(statusOrDefault(request.status()));
        return toResponse(permissionRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        SysPermission entity = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_NOT_FOUND));
        if (roleRepository.count() > 0 && permissionRepository.countRoleAssignments(id) > 0) {
            throw new BusinessException(ErrorCode.PERMISSION_IN_USE);
        }
        if (menuRepository.countByPermissionCode(entity.getCode()) > 0) {
            throw new BusinessException(ErrorCode.PERMISSION_IN_USE);
        }
        permissionRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }

    public PermissionResponse toResponse(SysPermission entity) {
        return new PermissionResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt());
    }

    private String statusOrDefault(String status) {
        return Statuses.isValid(status) ? status : Statuses.ACTIVE;
    }
}
