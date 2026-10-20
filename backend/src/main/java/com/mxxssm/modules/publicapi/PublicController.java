package com.mxxssm.modules.publicapi;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.modules.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public")
public class PublicController {

    private final AuthService authService;

    public PublicController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/settings")
    @Operation(summary = "Public site settings used by the login and registration pages")
    public ApiResponse<PublicSettingsResponse> settings() {
        return ApiResponse.ok(authService.publicSettings());
    }
}
