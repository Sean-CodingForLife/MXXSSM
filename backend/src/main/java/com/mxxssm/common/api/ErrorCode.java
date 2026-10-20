package com.mxxssm.common.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    OK(HttpStatus.OK),

    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    CSRF_INVALID(HttpStatus.FORBIDDEN),
    REGISTRATION_DISABLED(HttpStatus.FORBIDDEN),

    CAPTCHA_INVALID(HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST),
    PASSWORD_POLICY(HttpStatus.BAD_REQUEST),
    MENU_PERMISSION_INVALID(HttpStatus.BAD_REQUEST),
    SELF_OPERATION(HttpStatus.BAD_REQUEST),
    LAST_ADMIN(HttpStatus.CONFLICT),

    USERNAME_EXISTS(HttpStatus.CONFLICT),
    ROLE_CODE_EXISTS(HttpStatus.CONFLICT),
    PERMISSION_CODE_EXISTS(HttpStatus.CONFLICT),
    MENU_CODE_EXISTS(HttpStatus.CONFLICT),
    CONFIG_KEY_EXISTS(HttpStatus.CONFLICT),
    ROLE_IN_USE(HttpStatus.CONFLICT),
    PERMISSION_IN_USE(HttpStatus.CONFLICT),
    ROLE_BUILT_IN(HttpStatus.CONFLICT),
    CONFLICT(HttpStatus.CONFLICT),

    NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND),

    DATABASE_ERROR(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
