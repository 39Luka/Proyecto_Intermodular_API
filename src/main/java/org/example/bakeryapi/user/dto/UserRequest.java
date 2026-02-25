package org.example.bakeryapi.user.dto;

import org.example.bakeryapi.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role
) {}


