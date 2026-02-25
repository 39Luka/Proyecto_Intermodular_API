package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.auth.dto.LoginRequest;
import org.example.bakeryapi.auth.dto.RegisterRequest;
import org.example.bakeryapi.security.JwtProvider;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.example.bakeryapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_userRole_withoutAuth_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", Role.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        assertTrue(userRepository.existsByEmail("user@example.com"));
    }

    @Test
    void register_adminRole_withoutAuth_returnsForbidden() throws Exception {
        RegisterRequest request = new RegisterRequest("admin2@example.com", "password123", Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only an ADMIN can create another ADMIN"));
    }

    @Test
    void register_adminRole_withAdminToken_createsAdmin() throws Exception {
        User admin = new User("admin@example.com", passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);
        String token = jwtProvider.generateToken(admin.getEmail(), admin.getRole().name());

        RegisterRequest request = new RegisterRequest("newadmin@example.com", "password123", Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User created = userRepository.findByEmail("newadmin@example.com").orElseThrow();
        assertEquals(Role.ADMIN, created.getRole());
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("user@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}


