package com.mxxssm.modules.auth;

public record CaptchaResponse(String image, boolean enabled, int ttlSeconds) {
}
