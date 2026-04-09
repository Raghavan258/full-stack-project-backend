package com.constitution.backend.config;

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
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Know Your Constitution — API")
                        .description("""
                            REST API for the **Know Your Constitution** platform.
                            
                            ### Authentication
                            1. **Register** via `POST /api/auth/register` — an OTP is emailed.
                            2. **Verify OTP** via `POST /api/auth/verify-otp`.
                            3. **Login** via `POST /api/auth/login` — receive a JWT token.
                            4. Click **Authorize** and enter `Bearer <your-token>`.
                            
                            ### Roles
                            | Role | Access |
                            |------|--------|
                            | `citizen` | Read content, post forums |
                            | `educator` | + Create articles, quizzes, study notes |
                            | `legal_expert` | + Create articles |
                            | `admin` | Full access |
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Team Padma Vyuha")
                                .email("dev@constitution.in"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste the JWT token from /api/auth/login")));
    }
}
