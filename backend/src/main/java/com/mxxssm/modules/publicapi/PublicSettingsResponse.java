package com.mxxssm.modules.publicapi;

public record PublicSettingsResponse(
        String siteName,
        boolean captchaEnabled,
        boolean registrationEnabled,
        String defaultLocale,
        String version) {
}
