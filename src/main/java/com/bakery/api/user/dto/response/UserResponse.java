package com.bakery.api.user.dto.response;

import com.bakery.api.user.domain.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
}


