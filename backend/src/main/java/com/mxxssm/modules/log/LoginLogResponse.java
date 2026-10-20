package com.mxxssm.modules.log;

import java.time.LocalDateTime;

public record LoginLogResponse(
        Long id,
        String username,
        String status,
        String ip,
        String userAgent,
        String message,
        LocalDateTime createdAt) {
}
