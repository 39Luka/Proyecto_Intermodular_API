package com.bakery.api.auth.dto.response;

public record LoginResponse(
        String token,
        String refreshToken
) {
    public LoginResponse(String token) {
        this(token, null);
    }
}

