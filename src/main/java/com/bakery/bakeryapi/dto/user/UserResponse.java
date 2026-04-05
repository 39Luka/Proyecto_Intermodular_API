package com.bakery.bakeryapi.dto.user;

import com.bakery.bakeryapi.domain.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
}


