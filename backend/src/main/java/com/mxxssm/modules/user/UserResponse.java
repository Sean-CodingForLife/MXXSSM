package com.mxxssm.modules.user;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        String phone,
        String status,
        List<String> roles,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt) {
}
