package com.bakery.bakery_api.dto.response;

import com.bakery.bakery_api.domain.Usuario;

public record UsuarioDTO(
        Long id,
        String nombre,
        String email,
        Usuario.Rol rol
) {
    public static UsuarioDTO fromEntity(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol() // ya es enum
        );
    }
}
