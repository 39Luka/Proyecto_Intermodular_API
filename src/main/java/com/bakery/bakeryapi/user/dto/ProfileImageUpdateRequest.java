package com.bakery.bakeryapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de solicitud utilizado para actualizar o eliminar la imagen de perfil del usuario autenticado.
 *
 * @param profileImageBase64 imagen codificada como Base64, o {@code null}/en blanco para eliminar la imagen
 */
public record ProfileImageUpdateRequest(
        @Schema(description = "Profile image as base64. Send null or empty to remove it.", type = "string")
        String profileImageBase64
) {
}
