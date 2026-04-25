package com.bakery.bakeryapi.auth;

import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.infra.security.JwtTokenService;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.user.exception.UserDisabledException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userService, jwtTokenService, passwordEncoder);
    }

    @Test
    void register_createsUserAsUserRole_andReturnsTokens() {
        String email = "user@example.com";
        String password = "password123";

        User createdUser = new User(email, passwordEncoder.encode(password), Role.USER);
        createdUser.rotateRefreshToken();

        when(userService.createInternal(eq(email), eq(password), eq(Role.USER))).thenReturn(createdUser);
        when(userService.rotateRefreshToken(eq(createdUser))).thenReturn(createdUser);
        when(jwtTokenService.generateToken(eq(email), eq(Role.USER.name()))).thenReturn("jwt-token");
        when(jwtTokenService.generateRefreshToken(eq(email), eq(createdUser.getRefreshTokenVersion()))).thenReturn("refresh-token");
        when(jwtTokenService.getExpirationMs()).thenReturn(900000L);

        LoginResponse response = authService.register(email, password);

        assertEquals("jwt-token", response.token());
        assertEquals("refresh-token", response.refreshToken());
        verify(userService).createInternal(eq(email), eq(password), eq(Role.USER));
    }

    @Test
    void login_correctPassword_returnsTokens() {
        String email = "user@example.com";
        String password = "password123";
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, Role.USER);
        user.rotateRefreshToken();

        when(userService.getEntityByEmail(email)).thenReturn(user);
        when(userService.rotateRefreshToken(eq(user))).thenReturn(user);
        when(jwtTokenService.generateToken(email, Role.USER.name())).thenReturn("jwt-token");
        when(jwtTokenService.generateRefreshToken(eq(email), eq(user.getRefreshTokenVersion()))).thenReturn("refresh-token");
        when(jwtTokenService.getExpirationMs()).thenReturn(900000L);

        LoginResponse response = authService.login(email, password);

        assertEquals("jwt-token", response.token());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        String email = "user@example.com";
        User user = new User(email, passwordEncoder.encode("correctPassword"), Role.USER);

        when(userService.getEntityByEmail(email)).thenReturn(user);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(email, "wrongPassword"));

        verify(jwtTokenService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_userNotFound_throwsInvalidCredentialsException() {
        String email = "missing@example.com";

        when(userService.getEntityByEmail(email)).thenThrow(new UserNotFoundException(email));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(email, "anyPassword"));

        verify(jwtTokenService, never()).generateToken(any(), any());
    }

    @Test
    void login_disabledUser_throwsUserDisabledException() {
        String email = "user@example.com";
        String password = "password123";
        User user = new User(email, passwordEncoder.encode(password), Role.USER);
        user.disable();

        when(userService.getEntityByEmail(email)).thenReturn(user);

        assertThrows(UserDisabledException.class,
                () -> authService.login(email, password));

        verify(jwtTokenService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void refresh_validToken_rotatesRefreshToken() {
        String email = "user@example.com";
        User currentUser = new User(email, passwordEncoder.encode("password123"), Role.USER);
        currentUser.rotateRefreshToken();
        long previousVersion = currentUser.getRefreshTokenVersion();
        User rotatedUser = new User(email, currentUser.getPassword(), Role.USER);
        rotatedUser.rotateRefreshToken();
        rotatedUser.rotateRefreshToken();
        long newVersion = rotatedUser.getRefreshTokenVersion();

        when(jwtTokenService.readRefreshToken("old-refresh"))
                .thenReturn(new JwtTokenService.RefreshTokenPayload(email, previousVersion));
        when(userService.getEntityByEmail(email)).thenReturn(currentUser);
        when(userService.rotateRefreshToken(eq(currentUser))).thenReturn(rotatedUser);
        when(jwtTokenService.generateToken(email, Role.USER.name())).thenReturn("new-access");
        when(jwtTokenService.generateRefreshToken(email, newVersion)).thenReturn("new-refresh");
        when(jwtTokenService.getExpirationMs()).thenReturn(900000L);

        LoginResponse response = authService.refreshAccessToken("old-refresh");

        assertEquals("new-access", response.token());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals(900000L, response.expiresIn());
        assertNotEquals("old-refresh", response.refreshToken());
    }

    @Test
    void refresh_reusedRotatedToken_throwsInvalidCredentialsException() {
        String email = "user@example.com";
        User user = new User(email, passwordEncoder.encode("password123"), Role.USER);
        user.rotateRefreshToken();

        when(jwtTokenService.readRefreshToken("old-refresh"))
                .thenReturn(new JwtTokenService.RefreshTokenPayload(email, user.getRefreshTokenVersion() - 1));
        when(userService.getEntityByEmail(email)).thenReturn(user);

        assertThrows(InvalidCredentialsException.class, () -> authService.refreshAccessToken("old-refresh"));
    }
}
