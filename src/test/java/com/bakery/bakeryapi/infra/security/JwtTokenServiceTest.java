package com.bakery.bakeryapi.infra.security;

import com.bakery.bakeryapi.infra.config.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtTokenServiceTest {

    @Test
    void generateToken_includesSubjectRoleAndExpiry() {
        String secret = "0123456789abcdef0123456789abcdef";
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        JwtTokenService service = new JwtTokenService(encoder, decoder, new JwtProperties(secret, 60_000, 604_800_000L));
        String token = service.generateToken("user@example.com", "ADMIN");

        Jwt decoded = decoder.decode(token);
        assertEquals("user@example.com", decoded.getSubject());
        assertEquals("ADMIN", decoded.getClaimAsString("role"));
        assertNotNull(decoded.getExpiresAt());
    }
}
