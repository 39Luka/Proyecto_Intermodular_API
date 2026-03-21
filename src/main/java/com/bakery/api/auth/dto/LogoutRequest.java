package com.bakery.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "Refresh token to revoke", example = "rft_...")
        @NotBlank String refreshToken
) {
}
