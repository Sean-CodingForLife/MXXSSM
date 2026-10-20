package com.mxxssm.modules.permission;

import java.time.LocalDateTime;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String description,
        String status,
        LocalDateTime createdAt) {
}
