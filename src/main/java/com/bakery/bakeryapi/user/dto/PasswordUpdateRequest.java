package com.bakery.bakeryapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de solicitud utilizado por un usuario autenticado para cambiar su propia contraseña.
 *
 * @param currentPassword contraseña actual utilizada para verificar la propiedad de la cuenta
 * @param newPassword nueva contraseña sin procesar, entre 8 y 72 caracteres
 */
public record PasswordUpdateRequest(
        @Schema(description = "Current password", example = "password123")
        @NotBlank String currentPassword,
        @Schema(description = "New password (8-72 chars)", example = "newPassword123")
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
