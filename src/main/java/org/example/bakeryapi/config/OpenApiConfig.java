package org.example.bakeryapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para documentación automática de la API REST.
 *
 * Swagger genera una interfaz web interactiva donde puedes:
 * - Ver todos los endpoints disponibles
 * - Probar las peticiones directamente desde el navegador
 * - Ver los modelos de datos (DTOs)
 *
 * Acceso: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define el esquema de seguridad JWT (Bearer token en header Authorization)
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Información general del proyecto
                .info(new Info()
                        .title("Bakery API")
                        .description("API REST para gestión de panadería con autenticación JWT. " +
                                "Permite gestionar categorías, productos, promociones y compras.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bakery Team")
                                .email("support@bakery.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Configuración de seguridad: todos los endpoints requieren JWT
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))

                // Define cómo se envía el token JWT
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT") // Token en formato: "Bearer <token>"
                                        .description("Introduce el token JWT obtenido del endpoint /auth/login")));
    }
}
