package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta de token devuelta después del inicio de sesión, registro o actualización.
 */
public record LoginResponse(
        @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        @Schema(description = "Token de refresco para obtener nuevos tokens de acceso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        @Schema(description = "Tiempo de expiración del token en milisegundos", example = "900000")
        Long expiresIn
) {
}
