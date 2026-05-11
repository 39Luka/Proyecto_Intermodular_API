package com.bakery.bakeryapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used by an authenticated user to change their own password.
 *
 * @param currentPassword current password used to verify ownership of the account
 * @param newPassword new raw password, between 8 and 72 characters
 */
public record PasswordUpdateRequest(
        @Schema(description = "Current password", example = "password123")
        @NotBlank String currentPassword,
        @Schema(description = "New password (8-72 chars)", example = "newPassword123")
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
