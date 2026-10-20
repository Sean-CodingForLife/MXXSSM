package com.mxxssm.modules.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfigRequest(
        @NotBlank(message = "{validation.configKey}")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9._-]{1,127}$", message = "{validation.configKey}")
        String configKey,

        @NotBlank(message = "{validation.configValue}")
        @Size(max = 500, message = "{validation.configValue}")
        String configValue,

        @Size(max = 255, message = "{validation.configValue}")
        String description,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
