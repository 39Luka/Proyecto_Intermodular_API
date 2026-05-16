package com.bakery.bakeryapi.user.dto;

import com.bakery.bakeryapi.domain.Role;

import java.util.Base64;

/**
 * Datos de usuario devueltos por la API.
 *
 * @param id identificador de usuario
 * @param email correo electrónico único del usuario
 * @param role rol de usuario asignado
 * @param enabled si la cuenta puede autenticarse
 * @param profileImageBase64 imagen de perfil codificada como Base64, o {@code null} cuando está ausente
 */
public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled,
        String profileImageBase64
) {
    /**
     * Crea una respuesta de API a partir de una entidad de usuario.
     *
     * @param user entidad de usuario a convertir
     * @return DTO de respuesta con la imagen de perfil codificada como Base64 cuando está presente
     */
    public static UserResponse from(com.bakery.bakeryapi.domain.User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getProfileImage() == null ? null : Base64.getEncoder().encodeToString(user.getProfileImage())
        );
    }
}


