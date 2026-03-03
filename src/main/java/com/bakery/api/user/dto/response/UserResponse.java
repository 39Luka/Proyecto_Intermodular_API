package com.bakery.api.user.dto.response;

import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}


