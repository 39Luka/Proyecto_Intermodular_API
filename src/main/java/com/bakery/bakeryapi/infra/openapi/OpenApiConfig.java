package com.bakery.bakeryapi.infra.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * OpenAPI/Swagger configuration.
 *
 * Swagger UI:
 * - http://localhost:8080/swagger-ui/index.html
 *
 * OpenAPI JSON:
 * - http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Bakery API")
                        .description("REST API for a bakery with JWT auth. Manages categories, products, percentage promotions and purchases.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Use the JWT from POST /auth/login in the Authorization header: Bearer <token>")));
    }

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        final Content genericJsonContent = new Content()
                .addMediaType("application/json",
                        new MediaType().schema(new Schema<>().type("object")));

        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        ApiResponses responses = operation.getResponses();
                        if (responses == null) {
                            responses = new ApiResponses();
                            operation.setResponses(responses);
                        }

                        addIfMissing(responses, "400", "Bad request", genericJsonContent);
                        addIfMissing(responses, "500", "Internal server error", genericJsonContent);

                        boolean isPublicOperation = operation.getSecurity() != null && operation.getSecurity().isEmpty();
                        if (!isPublicOperation) {
                            addIfMissing(responses, "401", "Unauthorized", genericJsonContent);
                            addIfMissing(responses, "403", "Forbidden", genericJsonContent);
                        }
                    }));
        };
    }

    private static void addIfMissing(ApiResponses responses, String code, String description, Content content) {
        if (responses.containsKey(code)) {
            return;
        }
        responses.addApiResponse(code, new ApiResponse().description(description).content(content));
    }
}
