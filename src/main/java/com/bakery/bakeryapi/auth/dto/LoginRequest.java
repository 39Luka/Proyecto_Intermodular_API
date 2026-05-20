package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de solicitud para el inicio de sesión basado en contraseña.
 */
public record LoginRequest(
        @Schema(description = "Correo electrónico del usuario", example = "user@example.com")
        @Email @NotBlank String email,
        @Schema(description = "Contraseña del usuario", example = "password123")
        @NotBlank String password
) {}

