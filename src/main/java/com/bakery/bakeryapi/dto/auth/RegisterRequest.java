package com.bakery.bakeryapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(description = "Email for the new account", example = "user@example.com")
        @Email @NotBlank String email,
        @Schema(description = "Password (8-72 chars)", example = "password123")
        @NotBlank @Size(min = 8, max = 72) String password
) {}


