package com.mxxssm.modules.auth;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.common.web.MessageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final MessageHelper messages;

    public AuthController(AuthService authService, MessageHelper messages) {
        this.authService = authService;
        this.messages = messages;
    }

    @GetMapping("/captcha")
    @Operation(summary = "Create a login captcha")
    public ApiResponse<CaptchaResponse> captcha(HttpSession session) {
        return ApiResponse.ok(authService.captcha(session));
    }

    @GetMapping("/csrf")
    @Operation(summary = "Expose the CSRF token for the SPA")
    public ApiResponse<CsrfResponse> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            return ApiResponse.ok(new CsrfResponse(null, null, null));
        }
        return ApiResponse.ok(new CsrfResponse(token.getToken(), token.getHeaderName(), token.getParameterName()));
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in with username, password and optional captcha")
    public ApiResponse<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        CurrentUserResponse user = authService.login(request, httpRequest, httpResponse);
        return ApiResponse.ok(messages.get("message.login.success"), user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Sign out and invalidate the session")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext()
                .getAuthentication());
        SecurityContextHolder.clearContext();
        return ApiResponse.ok(messages.get("message.logout.success"), null);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a standard user account")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok(messages.get("message.register.success"), null);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current user, permissions and menus")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('user:profile:update')")
    @Operation(summary = "Update the current user's profile")
    public ApiResponse<CurrentUserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        CurrentUserResponse user = authService.updateProfile(request);
        return ApiResponse.ok(messages.get("message.profile.updated"), user);
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('user:password:update')")
    @Operation(summary = "Change the current user's password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok(messages.get("message.password.updated"), null);
    }
}
