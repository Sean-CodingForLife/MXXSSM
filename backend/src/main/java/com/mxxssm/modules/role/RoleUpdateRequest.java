package com.mxxssm.modules.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotBlank(message = "{validation.name}")
        @Size(max = 64, message = "{validation.name}")
        String name,

        @Size(max = 255, message = "{validation.name}")
        String description,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
