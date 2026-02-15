package org.example.bakeryapi.user.dto;

import org.example.bakeryapi.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        Role role
) {}
