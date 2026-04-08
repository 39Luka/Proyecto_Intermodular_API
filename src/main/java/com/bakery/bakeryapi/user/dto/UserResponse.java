package com.bakery.bakeryapi.user.dto;

import com.bakery.bakeryapi.domain.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
}


