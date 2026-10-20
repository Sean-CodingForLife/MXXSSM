package com.mxxssm.modules.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "{validation.currentPassword}") String currentPassword,
        @NotBlank(message = "{validation.newPassword}")
        @Size(min = 8, max = 64, message = "{validation.newPassword}")
        String newPassword) {
}
