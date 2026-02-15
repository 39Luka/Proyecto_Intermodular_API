package org.example.bakeryapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.bakeryapi.user.Role;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role
) {}
