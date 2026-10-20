package com.mxxssm.modules.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserUpdateRequest(
        @NotBlank(message = "{validation.displayName}")
        @Size(min = 2, max = 64, message = "{validation.displayName}")
        String displayName,

        @Email(message = "{validation.email}")
        @Size(max = 128, message = "{validation.email}")
        String email,

        @Pattern(regexp = "^$|^[0-9+()\\- ]{5,32}$", message = "{validation.phone}")
        String phone,

        @NotEmpty(message = "{validation.roles}")
        Set<String> roles,

        @Pattern(regexp = "ACTIVE|DISABLED", message = "{validation.status}")
        String status) {
}
