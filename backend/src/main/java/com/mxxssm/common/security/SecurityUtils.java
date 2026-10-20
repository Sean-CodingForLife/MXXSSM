package com.mxxssm.common.security;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.exception.BusinessException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CustomUserDetails> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails);
        }
        return Optional.empty();
    }

    public static CustomUserDetails requireUser() {
        return currentUser().orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
    }

    public static Long currentUserId() {
        return requireUser().getId();
    }

    public static String currentUsername() {
        return currentUser().map(CustomUserDetails::getUsername).orElse("");
    }
}
