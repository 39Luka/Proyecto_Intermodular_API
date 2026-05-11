package com.bakery.bakeryapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body used to update or remove the authenticated user's profile image.
 *
 * @param profileImageBase64 image encoded as Base64, or {@code null}/blank to remove the image
 */
public record ProfileImageUpdateRequest(
        @Schema(description = "Profile image as base64. Send null or empty to remove it.", type = "string")
        String profileImageBase64
) {
}
