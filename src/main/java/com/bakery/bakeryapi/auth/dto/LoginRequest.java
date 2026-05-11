package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for password-based login.
 */
public record LoginRequest(
        @Schema(description = "User email", example = "user@example.com")
        @Email @NotBlank String email,
        @Schema(description = "User password", example = "password123")
        @NotBlank String password
) {}

