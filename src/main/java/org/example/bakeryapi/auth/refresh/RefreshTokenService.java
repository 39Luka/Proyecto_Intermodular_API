package org.example.bakeryapi.auth.refresh;

import org.example.bakeryapi.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository repository;

    @Value("${app.refresh-token.expiration-ms:2592000000}") // 30 days
    private long refreshTokenExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String issueFor(User user) {
        return issueFor(user, null, null);
    }

    @Transactional
    public String issueFor(User user, String ip, String userAgent) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(refreshTokenExpirationMs);

        // Generate a random opaque token (not a JWT).
        String raw = generateRawToken();
        String hash = sha256Hex(raw);

        try {
            repository.save(new RefreshToken(user, hash, now, expiresAt, now, ip, userAgent));
        } catch (DataIntegrityViolationException e) {
            // Extremely unlikely collision. Retry once.
            raw = generateRawToken();
            hash = sha256Hex(raw);
            repository.save(new RefreshToken(user, hash, now, expiresAt, now, ip, userAgent));
        }

        return raw;
    }

    @Transactional
    public RotationResult rotate(String rawRefreshToken) {
        return rotate(rawRefreshToken, null, null);
    }

    @Transactional
    public RotationResult rotate(String rawRefreshToken, String ip, String userAgent) {
        String hash = sha256Hex(rawRefreshToken);
        RefreshToken existing = repository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        Instant now = Instant.now();
        if (existing.isRevoked() || existing.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        existing.touch(now, ip, userAgent);
        existing.revoke(now);
        repository.save(existing);

        String newToken = issueFor(existing.getUser(), ip, userAgent);
        return new RotationResult(existing.getUser(), newToken);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        String hash = sha256Hex(rawRefreshToken);
        RefreshToken existing = repository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.isRevoked()) {
            existing.revoke(Instant.now());
            repository.save(existing);
        }
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> listForUser(User user) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public int revokeAllForUser(User user) {
        return repository.revokeAllForUserId(user.getId(), Instant.now());
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
