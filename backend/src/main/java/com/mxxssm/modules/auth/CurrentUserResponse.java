package com.mxxssm.modules.auth;

import com.mxxssm.modules.menu.MenuResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record CurrentUserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        String phone,
        LocalDateTime lastLoginAt,
        Set<String> roles,
        Set<String> permissions,
        List<MenuResponse> menus) {
}
