package com.bakery.bakeryapi.user.dto;

import com.bakery.bakeryapi.domain.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {
    public static UserResponse from(com.bakery.bakeryapi.domain.User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isEnabled());
    }
}


