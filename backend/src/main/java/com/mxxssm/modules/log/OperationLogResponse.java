package com.mxxssm.modules.log;

import java.time.LocalDateTime;

public record OperationLogResponse(
        Long id,
        String username,
        String action,
        String method,
        String path,
        String status,
        String message,
        Long durationMs,
        String ip,
        LocalDateTime createdAt) {
}
