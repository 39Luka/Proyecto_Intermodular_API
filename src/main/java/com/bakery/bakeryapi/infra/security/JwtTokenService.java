package com.bakery.bakeryapi.infra.security;

import com.bakery.bakeryapi.infra.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.expirationMs = properties.expiration();
        // Refresh token expires in 7 days (604800000 ms)
        this.refreshExpirationMs = properties.refreshExpiration() != null ? 
                properties.refreshExpiration() : 7 * 24 * 60 * 60 * 1000L;
    }

    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .claim("role", role)
                .claim("type", "access")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String generateRefreshToken(String email, long refreshTokenVersion) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshExpirationMs))
                .claim("type", "refresh")
                .claim("refreshTokenVersion", refreshTokenVersion)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public RefreshTokenPayload readRefreshToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            if (!"refresh".equals(jwt.getClaimAsString("type"))) {
                return null;
            }
            Object versionClaim = jwt.getClaim("refreshTokenVersion");
            if (!(versionClaim instanceof Number refreshTokenVersion)) {
                return null;
            }
            return new RefreshTokenPayload(jwt.getSubject(), refreshTokenVersion.longValue());
        } catch (JwtException e) {
            return null;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public record RefreshTokenPayload(String subject, long refreshTokenVersion) {
    }
}
