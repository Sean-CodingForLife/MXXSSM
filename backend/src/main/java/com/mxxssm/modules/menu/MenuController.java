package com.mxxssm.modules.menu;

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
@RequestMapping("/api/v1/admin/menus")
@Tag(name = "Admin - Menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:menu:read')")
    @Operation(summary = "List menus")
    public ApiResponse<PageResult<MenuResponse>> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(menuService.page(keyword, page, size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:menu:read')")
    @Operation(summary = "List all menus for selects")
    public ApiResponse<List<MenuResponse>> all() {
        return ApiResponse.ok(menuService.all());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:menu:create')")
    @AuditLog(action = "admin.menu.create")
    @Operation(summary = "Create a menu")
    public ApiResponse<MenuResponse> create(@Valid @RequestBody MenuRequest request) {
        return ApiResponse.ok(menuService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:menu:update')")
    @AuditLog(action = "admin.menu.update")
    @Operation(summary = "Update a menu")
    public ApiResponse<MenuResponse> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        return ApiResponse.ok(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:menu:delete')")
    @AuditLog(action = "admin.menu.delete")
    @Operation(summary = "Delete a menu")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.ok();
    }
}
