package com.bakery.bakeryapi.userss.dto;

import com.bakery.bakeryapi.userss.domain.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
}


