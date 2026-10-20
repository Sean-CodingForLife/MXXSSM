package com.mxxssm.modules.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{validation.username}")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$", message = "{validation.username}")
        String username,

        @NotBlank(message = "{validation.displayName}")
        @Size(min = 2, max = 64, message = "{validation.displayName}")
        String displayName,

        @NotBlank(message = "{validation.password}")
        @Size(min = 8, max = 64, message = "{validation.password}")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.password}")
        String password,

        @Email(message = "{validation.email}")
        @Size(max = 128, message = "{validation.email}")
        String email,

        @Pattern(regexp = "^$|^[0-9+()\\- ]{5,32}$", message = "{validation.phone}")
        String phone) {
}
