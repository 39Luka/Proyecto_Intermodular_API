package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        @Schema(description = "Refresh token (opaque)", example = "rft_...")
        String refreshToken
) {
    public LoginResponse(String token) {
        this(token, null);
    }
}

