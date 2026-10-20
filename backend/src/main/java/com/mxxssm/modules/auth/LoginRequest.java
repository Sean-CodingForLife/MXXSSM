package com.mxxssm.modules.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{validation.username}") String username,
        @NotBlank(message = "{validation.password}") String password,
        String captcha) {
}
