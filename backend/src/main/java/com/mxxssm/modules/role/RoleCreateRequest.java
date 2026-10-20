package com.mxxssm.modules.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank(message = "{validation.code}")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "{validation.code}")
        String code,

        @NotBlank(message = "{validation.name}")
        @Size(max = 64, message = "{validation.name}")
        String name,

        @Size(max = 255, message = "{validation.name}")
        String description,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
