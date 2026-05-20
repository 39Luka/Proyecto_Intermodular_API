package com.bakery.bakeryapi.infra.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Configuración de OpenAPI para metadatos de documentación, seguridad JWT y errores comunes.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Bakery API")
                        .description("API REST para una panadería con autenticación JWT. Gestiona categorías, productos, promociones de porcentaje y compras.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Usar el JWT de POST /auth/login en la cabecera Authorization: Bearer <token>")));
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

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) -> {

                        ApiResponses responses = operation.getResponses();
                        if (responses == null) {
                            responses = new ApiResponses();
                            operation.setResponses(responses);
                        }

                        addIfMissing(responses, "400", "Solicitud incorrecta", genericJsonContent);
                        addIfMissing(responses, "500", "Error interno del servidor", genericJsonContent);

                        boolean isSecuredOperation = operation.getSecurity() != null && !operation.getSecurity().isEmpty();
                        if (isSecuredOperation) {
                            addIfMissing(responses, "401", "No autorizado", genericJsonContent);
                            addIfMissing(responses, "403", "Prohibido", genericJsonContent);
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
