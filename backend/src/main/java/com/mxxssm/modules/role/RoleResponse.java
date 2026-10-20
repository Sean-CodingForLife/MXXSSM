package com.mxxssm.modules.role;

import java.time.LocalDateTime;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        String status,
        long userCount,
        LocalDateTime createdAt) {
}
