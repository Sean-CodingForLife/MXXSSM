package com.mxxssm.modules.config;

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
@RequestMapping("/api/v1/admin/configs")
@Tag(name = "Admin - System configuration")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:config:read')")
    @Operation(summary = "List configuration entries")
    public ApiResponse<PageResult<ConfigResponse>> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(configService.page(keyword, page, size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:config:read')")
    @Operation(summary = "List all configuration entries")
    public ApiResponse<List<ConfigResponse>> all() {
        return ApiResponse.ok(configService.all());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:config:create')")
    @AuditLog(action = "admin.config.create")
    @Operation(summary = "Create a configuration entry")
    public ApiResponse<ConfigResponse> create(@Valid @RequestBody ConfigRequest request) {
        return ApiResponse.ok(configService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:config:update')")
    @AuditLog(action = "admin.config.update")
    @Operation(summary = "Update a configuration entry")
    public ApiResponse<ConfigResponse> update(@PathVariable Long id, @Valid @RequestBody ConfigRequest request) {
        return ApiResponse.ok(configService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:config:delete')")
    @AuditLog(action = "admin.config.delete")
    @Operation(summary = "Delete a configuration entry")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResponse.ok();
    }
}
