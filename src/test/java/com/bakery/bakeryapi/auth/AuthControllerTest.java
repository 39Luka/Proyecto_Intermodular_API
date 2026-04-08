package com.bakery.bakeryapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.bakeryapi.auth.dto.LoginRequest;
import com.bakery.bakeryapi.auth.dto.RegisterRequest;
import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.shared.exception.GlobalExceptionHandler;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.auth.AuthService;
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

        when(authService.login(eq(request.email()), eq(request.password()))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).login(eq(request.email()), eq(request.password()));
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
        RegisterRequest request = new RegisterRequest("new@example.com", "pass12345");
        LoginResponse response = new LoginResponse("new.jwt.token");

        when(authService.register(eq(request.email()), eq(request.password())))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(response.token()));

        verify(authService).register(eq(request.email()), eq(request.password()));
    }

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

    @Test
    void register_invalidRequest_returnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}


