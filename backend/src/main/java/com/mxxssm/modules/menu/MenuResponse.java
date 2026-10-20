package com.mxxssm.modules.menu;

import java.time.LocalDateTime;

public record MenuResponse(
        Long id,
        String code,
        String name,
        String path,
        String icon,
        String groupName,
        String permissionCode,
        Integer sortOrder,
        String status,
        LocalDateTime createdAt) {
}
