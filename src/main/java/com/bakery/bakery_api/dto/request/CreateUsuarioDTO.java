package com.bakery.bakery_api.dto.request;

import com.bakery.bakery_api.domain.Usuario;

public record CreateUsuarioDTO(
        String nombre,
        String email,
        String contrasena,
        Usuario.Rol rol // ADMIN o CLIENTE
) {}
