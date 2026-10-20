package com.mxxssm.modules.role;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.audit.AuditLog;
import com.mxxssm.modules.permission.PermissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/roles")
@Tag(name = "Admin - Roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:role:read')")
    @Operation(summary = "List roles")
    public ApiResponse<PageResult<RoleResponse>> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(roleService.page(keyword, page, size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:role:read')")
    @Operation(summary = "List active roles for selects")
    public ApiResponse<List<RoleResponse>> all() {
        return ApiResponse.ok(roleService.active());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:role:read')")
    @Operation(summary = "Get a role")
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roleService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:role:create')")
    @AuditLog(action = "admin.role.create")
    @Operation(summary = "Create a role")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:role:update')")
    @AuditLog(action = "admin.role.update")
    @Operation(summary = "Update a role")
    public ApiResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:role:delete')")
    @AuditLog(action = "admin.role.delete")
    @Operation(summary = "Delete a role")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('admin:role:permission:read')")
    @Operation(summary = "List permissions assigned to a role")
    public ApiResponse<List<PermissionResponse>> permissions(@PathVariable Long id) {
        return ApiResponse.ok(roleService.permissions(id));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('admin:role:permission:update')")
    @AuditLog(action = "admin.role.permissions.update")
    @Operation(summary = "Replace the permissions assigned to a role")
    public ApiResponse<Void> updatePermissions(@PathVariable Long id,
            @Valid @RequestBody RolePermissionUpdateRequest request) {
        roleService.updatePermissions(id, request.permissionIds());
        return ApiResponse.ok();
    }
}
