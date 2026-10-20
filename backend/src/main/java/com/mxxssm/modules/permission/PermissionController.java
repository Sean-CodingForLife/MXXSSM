package com.mxxssm.modules.permission;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.audit.AuditLog;
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
@RequestMapping("/api/v1/admin/permissions")
@Tag(name = "Admin - Permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:permission:read')")
    @Operation(summary = "List permissions")
    public ApiResponse<PageResult<PermissionResponse>> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(permissionService.page(keyword, page, size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:permission:read')")
    @Operation(summary = "List active permissions for selects")
    public ApiResponse<List<PermissionResponse>> all() {
        return ApiResponse.ok(permissionService.active());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:permission:create')")
    @AuditLog(action = "admin.permission.create")
    @Operation(summary = "Create a permission")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ApiResponse.ok(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:permission:update')")
    @AuditLog(action = "admin.permission.update")
    @Operation(summary = "Update a permission")
    public ApiResponse<PermissionResponse> update(@PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return ApiResponse.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:permission:delete')")
    @AuditLog(action = "admin.permission.delete")
    @Operation(summary = "Delete a permission")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.ok();
    }
}
