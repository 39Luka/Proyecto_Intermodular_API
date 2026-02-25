package org.example.bakeryapi.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "12345678901234567890123456789012",
                3600000
        );
    }

    @Test
    void generateToken_returnsValidToken() {
        String email = "test@example.com";
        String role = "USER";

        String token = jwtProvider.generateToken(email, role);

        assertNotNull(token);

        assertDoesNotThrow(() -> jwtProvider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_throwsJwtException() {
        String invalidToken = "this.is.not.valid";

        assertThrows(JwtException.class, () -> jwtProvider.validateToken(invalidToken));
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String email = "test@example.com";
        String role = "USER";

        String token = jwtProvider.generateToken(email, role);

        String extractedEmail = jwtProvider.getEmailFromToken(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void getRoleFromToken_returnsCorrectRole() {
        String email = "test@example.com";
        String role = "ADMIN";

        String token = jwtProvider.generateToken(email, role);

        String extractedRole = jwtProvider.getRoleFromToken(token);
        assertEquals(role, extractedRole);
    }
}


