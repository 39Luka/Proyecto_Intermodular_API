package org.example.bakeryapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.auth.dto.request.LoginRequest;
import org.example.bakeryapi.auth.dto.request.RegisterRequest;
import org.example.bakeryapi.auth.dto.response.LoginResponse;
import org.example.bakeryapi.auth.refresh.RefreshTokenService;
import org.example.bakeryapi.common.exception.GlobalExceptionHandler;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenService refreshTokenService;

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

    @Test
    void login_validRequest_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        LoginResponse response = new LoginResponse("fake.jwt.token");

        when(authService.login(eq(request.email()), eq(request.password()), any(), any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).login(eq(request.email()), eq(request.password()), any(), any());
    }

    @Test
    void login_invalidRequest_returnsBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_validRequest_returnsCreatedToken() throws Exception {
        RegisterRequest request = new RegisterRequest("new@example.com", "pass12345", Role.USER);
        LoginResponse response = new LoginResponse("new.jwt.token");

        when(authService.register(eq(request.email()), eq(request.password()), eq(request.role()), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).register(eq(request.email()), eq(request.password()), eq(request.role()), any(), any());
    }

    @Test
    void register_existingEmail_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@example.com", "pass12345", Role.USER);

        when(authService.register(anyString(), anyString(), any(), any(), any()))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(authService).register(eq(request.email()), eq(request.password()), eq(request.role()), any(), any());
    }

    @Test
    void register_invalidRequest_returnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", null, null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}


