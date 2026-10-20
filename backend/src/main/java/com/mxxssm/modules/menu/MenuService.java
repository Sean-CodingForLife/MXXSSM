package com.mxxssm.modules.menu;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.domain.Statuses;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.web.Pageables;
import com.mxxssm.modules.permission.SysPermissionRepository;
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
public class MenuService {

    private final SysMenuRepository menuRepository;
    private final SysPermissionRepository permissionRepository;

    public MenuService(SysMenuRepository menuRepository, SysPermissionRepository permissionRepository) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<MenuResponse> page(String keyword, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by("id")));
        Specification<SysMenu> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("path")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(menuRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> all() {
        return menuRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> active() {
        return menuRepository.findByStatusOrderBySortOrderAscIdAsc(Statuses.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> forPermissions(java.util.Collection<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        return menuRepository.findByPermissionCodeInAndStatusOrderBySortOrderAscIdAsc(permissionCodes, Statuses.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MenuResponse create(MenuRequest request) {
        String code = request.code().trim();
        if (menuRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.MENU_CODE_EXISTS, code);
        }
        validatePermission(request.permissionCode());
        SysMenu entity = new SysMenu();
        entity.setCode(code);
        apply(entity, request);
        return toResponse(menuRepository.save(entity));
    }

    @Transactional
    public MenuResponse update(Long id, MenuRequest request) {
        SysMenu entity = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        String code = request.code().trim();
        if (!code.equals(entity.getCode())) {
            if (menuRepository.existsByCode(code)) {
                throw new BusinessException(ErrorCode.MENU_CODE_EXISTS, code);
            }
            entity.setCode(code);
        }
        validatePermission(request.permissionCode());
        apply(entity, request);
        return toResponse(menuRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        SysMenu entity = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        menuRepository.delete(entity);
    }

    private void apply(SysMenu entity, MenuRequest request) {
        entity.setName(request.name().trim());
        entity.setPath(request.path().trim());
        entity.setIcon(request.icon());
        entity.setGroupName(request.groupName().trim());
        entity.setPermissionCode(request.permissionCode().trim());
        entity.setSortOrder(request.sortOrder());
        entity.setStatus(Statuses.isValid(request.status()) ? request.status() : Statuses.ACTIVE);
    }

    private void validatePermission(String permissionCode) {
        if (!permissionRepository.existsByCode(permissionCode)) {
            throw new BusinessException(ErrorCode.MENU_PERMISSION_INVALID);
        }
    }

    public MenuResponse toResponse(SysMenu entity) {
        return new MenuResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getPath(),
                entity.getIcon(),
                entity.getGroupName(),
                entity.getPermissionCode(),
                entity.getSortOrder(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
