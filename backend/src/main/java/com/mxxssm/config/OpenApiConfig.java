package com.mxxssm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SESSION_COOKIE = "sessionCookie";

    @Bean
    public OpenAPI mxxSsmOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MXXSSM API")
                        .version("2.0.0")
                        .description("Session-based RBAC administration API.")
                        .contact(new Contact().name("MXXSSM").url("https://github.com/Sean-CodingForLife/MXXSSM"))
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes(SESSION_COOKIE,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("MXXSSM_SESSION")))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE));
    }
}
