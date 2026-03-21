package com.bakery.api.auth;

import com.bakery.api.auth.dto.LoginResponse;
import com.bakery.api.auth.exception.InvalidCredentialsException;
import com.bakery.api.auth.refresh.RefreshTokenService;
import com.bakery.api.security.JwtTokenService;
import com.bakery.api.user.UserService;
import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
import com.bakery.api.user.exception.UserDisabledException;
import com.bakery.api.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock
    private RefreshTokenService refreshTokenService;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userService, jwtTokenService, passwordEncoder, refreshTokenService);
    }

    @Test
    void register_createsUserAsUserRole_andReturnsTokens() {
        String email = "user@example.com";
        String password = "password123";

        User createdUser = new User(email, passwordEncoder.encode(password), Role.USER);

        when(userService.createInternal(eq(email), eq(password), eq(Role.USER))).thenReturn(createdUser);
        when(jwtTokenService.generateToken(eq(email), eq(Role.USER.name()))).thenReturn("jwt-token");
        when(refreshTokenService.issueFor(eq(createdUser))).thenReturn("refresh-token");

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

        when(userService.getEntityByEmail(email)).thenReturn(user);
        when(jwtTokenService.generateToken(email, Role.USER.name())).thenReturn("jwt-token");
        when(refreshTokenService.issueFor(eq(user))).thenReturn("refresh-token");

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
}
