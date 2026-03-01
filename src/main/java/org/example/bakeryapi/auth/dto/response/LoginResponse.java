package org.example.bakeryapi.auth.dto.response;

public record LoginResponse(
        String token,
        String refreshToken
) {
    public LoginResponse(String token) {
        this(token, null);
    }
}

