package com.bakery.bakeryapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de solicitud utilizado para actualizar o eliminar la imagen de perfil del usuario autenticado.
 *
 * @param profileImageBase64 imagen codificada como Base64, o {@code null}/en blanco para eliminar la imagen
 */
public record ProfileImageUpdateRequest(
        @Schema(description = "Imagen de perfil en formato base64. Enviar null o vacío para eliminarla.", type = "string")
        String profileImageBase64
) {
}
