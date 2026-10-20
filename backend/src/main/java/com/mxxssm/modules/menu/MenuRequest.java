package com.mxxssm.modules.menu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MenuRequest(
        @NotBlank(message = "{validation.code}")
        @Pattern(regexp = "^[a-z][a-z0-9-]{1,63}$", message = "{validation.code}")
        String code,

        @NotBlank(message = "{validation.name}")
        @Size(max = 128, message = "{validation.name}")
        String name,

        @NotBlank(message = "{validation.path}")
        @Pattern(regexp = "^/[A-Za-z0-9/_\\-]*$", message = "{validation.path}")
        @Size(max = 255, message = "{validation.path}")
        String path,

        @Size(max = 64, message = "{validation.name}")
        String icon,

        @NotBlank(message = "{validation.name}")
        @Size(max = 64, message = "{validation.name}")
        String groupName,

        @NotBlank(message = "{validation.code}")
        @Size(max = 64, message = "{validation.code}")
        String permissionCode,

        @NotNull(message = "{validation.sortOrder}")
        @Min(value = 0, message = "{validation.sortOrder}")
        Integer sortOrder,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
