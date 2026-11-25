package ch.accenture.academyhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final Set<PathItem.HttpMethod> SAFE_METHODS = Set.of(
            PathItem.HttpMethod.GET, PathItem.HttpMethod.HEAD, PathItem.HttpMethod.OPTIONS
    );

    @Bean
    OpenAPI academyHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Academy Hub API")
                        .version("0.0.1-SNAPSHOT")
                        .description("""
                                Session-based Academy Hub API. First execute GET /api/auth/csrf.
                                Copy the response token into Authorize > csrfToken, then execute
                                POST /api/auth/login with your local account. The browser manages
                                JSESSIONID automatically; do not paste a session cookie into Authorize.
                                Execute GET /api/auth/me to check the current account.
                                After logout or a CSRF error, retrieve and authorise a fresh token.
                                """))
                .components(new Components()
                        .addSecuritySchemes("sessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Managed by the browser after login; leave this input empty."))
                        .addSecuritySchemes("csrfToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")
                                .description("Paste token from GET /api/auth/csrf, not the raw cookie value.")));
    }

    @Bean
    OpenApiCustomizer documentSessionAndCsrfRequirements() {
        return api -> {
            if (api.getPaths() == null) {
                return;
            }
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                boolean requiresSession = !path.equals("/api/auth/csrf")
                        && !path.equals("/api/auth/login");
                boolean requiresCsrf = !SAFE_METHODS.contains(method);

                SecurityRequirement requirement = new SecurityRequirement();
                if (requiresSession) {
                    requirement.addList("sessionCookie");
                }
                if (requiresCsrf) {
                    requirement.addList("csrfToken");
                }
                operation.setSecurity(requirement.isEmpty()
                        ? new ArrayList<>() : List.of(requirement));

                if (operation.getResponses() == null) {
                    operation.setResponses(new ApiResponses());
                }
                if (requiresSession) {
                    operation.getResponses().putIfAbsent("401",
                            new ApiResponse().description("Authentication is required."));
                }
                if (requiresCsrf || requiresSession) {
                    operation.getResponses().putIfAbsent("403", new ApiResponse()
                            .description("Access denied, or a required CSRF token is missing or invalid."));
                }
            }));
        };
    }
}
