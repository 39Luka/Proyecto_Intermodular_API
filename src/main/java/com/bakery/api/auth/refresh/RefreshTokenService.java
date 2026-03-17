package com.bakery.api.auth.refresh;

import com.bakery.api.config.properties.RefreshTokenProperties;
import com.bakery.api.user.domain.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    /**
     * Refresh token issuance/rotation/revocation (hashed, opaque tokens).
     *
     * ADR: docs/adr/0001-jwt-access-refresh-tokens.md
     */
    private static final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;

    public RefreshTokenService(RefreshTokenRepository repository, RefreshTokenProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public String issueFor(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.expirationMs());

        // Generate a random opaque token (not a JWT). Only return the raw token to the client.
        String raw = generateRawToken();
        String hash = sha256Hex(raw);

        try {
            repository.save(new RefreshToken(user, hash, now, expiresAt));
        } catch (DataIntegrityViolationException e) {
            // Extremely unlikely collision. Retry once.
            raw = generateRawToken();
            hash = sha256Hex(raw);
            repository.save(new RefreshToken(user, hash, now, expiresAt));
        }

        return raw;
    }

    @Transactional
    public RotationResult rotate(String rawRefreshToken) {
        // Look up by hash so we never have to store raw tokens server-side.
        String hash = sha256Hex(rawRefreshToken);
        RefreshToken existing = repository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        Instant now = Instant.now();
        if (existing.isRevoked() || existing.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        // Revoke first, then issue a new one. If anything fails, the transaction rolls back.
        existing.revoke(now);
        repository.save(existing);

        String newToken = issueFor(existing.getUser());
        return new RotationResult(existing.getUser(), newToken);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        // Logout: mark the token as revoked so it can't be used again.
        String hash = sha256Hex(rawRefreshToken);
        RefreshToken existing = repository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.isRevoked()) {
            existing.revoke(Instant.now());
            repository.save(existing);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32]; // 256-bit
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record RotationResult(User user, String refreshToken) {
    }
}
