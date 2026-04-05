package com.bakery.bakeryapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "Refresh token obtained from login/register", example = "rft_...")
        @NotBlank String refreshToken
) {
}
