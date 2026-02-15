package org.example.bakeryapi.auth;

import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.auth.exception.InvalidCredentialsException;
import org.example.bakeryapi.security.JwtProvider;
import org.example.bakeryapi.user.Role;
import org.example.bakeryapi.user.User;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userService, jwtProvider, passwordEncoder);
    }


    @Test
    void register_userRole_createsUserAndReturnsToken() {
        String email = "user@example.com";
        String password = "123456";
        Role role = Role.USER;

        User createdUser = new User(email, passwordEncoder.encode(password), role);

        when(userService.create(anyString(), anyString(), eq(role))).thenReturn(createdUser);
        when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("fake-jwt-token");

        String token = authService.register(email, password, role);

        assertEquals("fake-jwt-token", token);
        verify(userService).create(eq(email), anyString(), eq(role));
        verify(jwtProvider).generateToken(email, role.name());
    }

    @Test
    void register_adminRoleWithoutAdminAuth_throwsForbiddenOperationException() {
        String email = "admin@example.com";
        String password = "admin123";
        Role role = Role.ADMIN;

        SecurityContextHolder.setContext(mock(SecurityContext.class));
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);

        assertThrows(ForbiddenOperationException.class,
                () -> authService.register(email, password, role));

        SecurityContextHolder.clearContext();
    }
    @Test
    void register_adminRoleWithAdminAuth_createsAdminAndReturnsToken() {
        String email = "admin@example.com";
        String password = "admin123";
        Role role = Role.ADMIN;

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User createdUser = new User(email, passwordEncoder.encode(password), role);
        when(userService.create(anyString(), anyString(), eq(role))).thenReturn(createdUser);
        when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        String token = authService.register(email, password, role);

        assertEquals("jwt-token", token);
        verify(userService).create(eq(email), anyString(), eq(role));
        verify(jwtProvider).generateToken(email, role.name());

        SecurityContextHolder.clearContext();
    }


    @Test
    void login_correctPassword_returnsToken() {
        String email = "user@example.com";
        String password = "123456";
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, Role.USER);

        when(userService.getByEmail(email)).thenReturn(user);
        when(jwtProvider.generateToken(email, Role.USER.name())).thenReturn("jwt-token");

        String token = authService.login(email, password);

        assertEquals("jwt-token", token);
        verify(userService).getByEmail(email);
        verify(jwtProvider).generateToken(email, Role.USER.name());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        String email = "user@example.com";
        User user = new User(email, passwordEncoder.encode("correctPassword"), Role.USER);

        when(userService.getByEmail(email)).thenReturn(user);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(email, "wrongPassword"));

        verify(userService).getByEmail(email);
        verify(jwtProvider, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_userNotFound_throwsInvalidCredentialsException() {
        String email = "missing@example.com";

        when(userService.getByEmail(email)).thenThrow(new UserNotFoundException(email));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(email, "anyPassword"));

        verify(userService).getByEmail(email);
        verify(jwtProvider, never()).generateToken(any(), any());
    }


    @Test
    void register_userRole_withoutAuthentication_stillWorks() {
        String email = "user@example.com";
        String password = "123456";
        Role role = Role.USER;

        SecurityContextHolder.clearContext();

        User createdUser = new User(email, passwordEncoder.encode(password), role);
        when(userService.create(anyString(), anyString(), eq(role))).thenReturn(createdUser);
        when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        String token = authService.register(email, password, role);

        assertEquals("jwt-token", token);
    }

    @Test
    void register_adminRoleWithNonAdminAuth_throwsForbiddenOperationException() {
        String email = "admin@example.com";
        String password = "admin123";

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(ForbiddenOperationException.class,
                () -> authService.register(email, password, Role.ADMIN));

        SecurityContextHolder.clearContext();
    }



}
