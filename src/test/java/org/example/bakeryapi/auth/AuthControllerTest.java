package org.example.bakeryapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.auth.dto.LoginRequest;
import org.example.bakeryapi.auth.dto.LoginResponse;
import org.example.bakeryapi.auth.dto.RegisterRequest;
import org.example.bakeryapi.common.exception.GlobalExceptionHandler;
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

        when(authService.login(request.email(), request.password())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).login(request.email(), request.password());
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
        RegisterRequest request = new RegisterRequest("new@example.com", "pass123", Role.USER);
        LoginResponse response = new LoginResponse("new.jwt.token");

        when(authService.register(request.email(), request.password(), request.role())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).register(request.email(), request.password(), request.role());
    }

    @Test
    void register_existingEmail_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@example.com", "pass123", Role.USER);

        when(authService.register(anyString(), anyString(), any()))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(authService).register(request.email(), request.password(), request.role());
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


