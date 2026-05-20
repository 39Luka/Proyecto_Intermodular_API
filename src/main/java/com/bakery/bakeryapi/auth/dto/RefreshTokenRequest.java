package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de solicitud utilizado para intercambiar un token de refresco por un nuevo par de tokens.
 */
public record RefreshTokenRequest(
        @Schema(description = "Token de refresco", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "El token de refresco no puede estar en blanco")
        String refreshToken
) {
}
