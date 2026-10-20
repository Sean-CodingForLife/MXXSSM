package com.mxxssm.modules.auth;

public record CsrfResponse(String token, String headerName, String parameterName) {
}
