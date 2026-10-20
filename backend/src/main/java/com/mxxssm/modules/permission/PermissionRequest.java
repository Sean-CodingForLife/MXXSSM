package com.mxxssm.modules.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PermissionRequest(
        @NotBlank(message = "{validation.code}")
        @Pattern(regexp = "^[a-z][a-z0-9:_-]{2,63}$", message = "{validation.code}")
        String code,

        @NotBlank(message = "{validation.name}")
        @Size(max = 128, message = "{validation.name}")
        String name,

        @Size(max = 255, message = "{validation.name}")
        String description,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
