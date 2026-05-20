package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de solicitud para el registro público de cuentas.
 */
public record RegisterRequest(
        @Schema(description = "Correo electrónico para la nueva cuenta", example = "user@example.com")
        @Email @NotBlank String email,
        @Schema(description = "Contraseña (8-72 caracteres)", example = "password123")
        @NotBlank @Size(min = 8, max = 72) String password
) {}


