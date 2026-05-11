package com.bakery.bakeryapi.user.dto;

import com.bakery.bakeryapi.domain.Role;

import java.util.Base64;

/**
 * User data returned by the API.
 *
 * @param id user identifier
 * @param email unique user email
 * @param role assigned user role
 * @param enabled whether the account can authenticate
 * @param profileImageBase64 profile image encoded as Base64, or {@code null} when absent
 */
public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled,
        String profileImageBase64
) {
    /**
     * Creates an API response from a user entity.
     *
     * @param user user entity to convert
     * @return response DTO with the profile image encoded as Base64 when present
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


