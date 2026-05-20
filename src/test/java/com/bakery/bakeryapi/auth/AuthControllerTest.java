package com.bakery.bakeryapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.bakeryapi.auth.dto.LoginRequest;
import com.bakery.bakeryapi.auth.dto.RegisterRequest;
import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.shared.exception.GlobalExceptionHandler;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * CP-AUT.01: login_validRequest_returnsToken
     * Valida que un login con credenciales correctas devuelva un token JWT y los datos de expiración.
     */
    @Test
    void login_validRequest_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        LoginResponse response = new LoginResponse("fake.jwt.token", "fake.refresh.token", 900000L);

        when(authService.login(eq(request.email()), eq(request.password()))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).login(eq(request.email()), eq(request.password()));
    }

    /**
     * CP-AUT.02: login_invalidRequest_returnsBadRequest
     * Verifica que si los campos del login están vacíos o son nulos, la API responda con error 400.
     */
    @Test
    void login_invalidRequest_returnsBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * CP-AUT.03: register_validRequest_returnsCreatedToken
     * Valida el registro exitoso de un nuevo usuario y la generación inmediata de su token de acceso.
     */
    @Test
    void register_validRequest_returnsCreatedToken() throws Exception {
        RegisterRequest request = new RegisterRequest("new@example.com", "pass12345");
        LoginResponse response = new LoginResponse("new.jwt.token", "new.refresh.token", 900000L);

        when(authService.register(eq(request.email()), eq(request.password())))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).register(eq(request.email()), eq(request.password()));
    }

    /**
     * CP-AUT.04: register_existingEmail_returnsConflict
     * Asegura que el sistema no permita registrar dos cuentas con el mismo correo electrónico (Error 409).
     */
    @Test
    void register_existingEmail_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@example.com", "pass12345");

        when(authService.register(anyString(), anyString()))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(authService).register(eq(request.email()), eq(request.password()));
    }

    /**
     * CP-AUT.05: register_invalidRequest_returnsBadRequest
     * Verifica la validación de campos obligatorios en el registro de usuarios.
     */
    @Test
    void register_invalidRequest_returnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * CP-AUT.06: getMe_authenticated_returnsCurrentUser
     * Valida que el endpoint /me devuelva la información del usuario actualmente autenticado basado en su token.
     */
    @Test
    void getMe_authenticated_returnsCurrentUser() throws Exception {
        setAuth("user@example.com");
        UserResponse response = new UserResponse(1L, "user@example.com", Role.USER, true, null);
        when(userService.getByEmail("user@example.com")).thenReturn(response);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getByEmail("user@example.com");
    }

    private void setAuth(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}


