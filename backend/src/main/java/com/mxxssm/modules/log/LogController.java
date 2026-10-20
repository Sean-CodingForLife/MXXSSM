package com.mxxssm.modules.log;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/logs")
@Tag(name = "Admin - Audit logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority('admin:log:login:read')")
    @Operation(summary = "List login audit records")
    public ApiResponse<PageResult<LoginLogResponse>> loginLogs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(logService.pageLoginLogs(keyword, status, page, size));
    }

    @GetMapping("/operation")
    @PreAuthorize("hasAuthority('admin:log:operation:read')")
    @Operation(summary = "List operation audit records")
    public ApiResponse<PageResult<OperationLogResponse>> operationLogs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(logService.pageOperationLogs(keyword, status, page, size));
    }
}
