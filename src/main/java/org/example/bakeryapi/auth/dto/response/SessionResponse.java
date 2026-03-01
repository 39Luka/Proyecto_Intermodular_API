package org.example.bakeryapi.auth.dto.response;

import org.example.bakeryapi.auth.refresh.RefreshToken;

import java.time.Instant;

public record SessionResponse(
        Long id,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        Instant revokedAt,
        String ip,
        String userAgent
) {
    public static SessionResponse from(RefreshToken token) {
        return new SessionResponse(
                token.getId(),
                token.getCreatedAt(),
                token.getLastUsedAt(),
                token.getExpiresAt(),
                token.getRevokedAt(),
                token.getIp(),
                token.getUserAgent()
        );
    }
}
