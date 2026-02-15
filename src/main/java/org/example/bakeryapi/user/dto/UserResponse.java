package org.example.bakeryapi.user.dto;

import org.example.bakeryapi.user.Role;
import org.example.bakeryapi.user.User;

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
