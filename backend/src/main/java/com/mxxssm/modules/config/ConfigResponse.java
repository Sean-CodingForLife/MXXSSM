package com.mxxssm.modules.config;

import java.time.LocalDateTime;

public record ConfigResponse(
        Long id,
        String configKey,
        String configValue,
        String description,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
