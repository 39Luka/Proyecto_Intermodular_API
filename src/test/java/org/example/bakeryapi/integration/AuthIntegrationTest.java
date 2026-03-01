package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.bakeryapi.auth.dto.request.LoginRequest;
import org.example.bakeryapi.auth.dto.request.LogoutRequest;
import org.example.bakeryapi.auth.dto.request.RefreshRequest;
import org.example.bakeryapi.auth.dto.request.RegisterRequest;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void register_userRole_withoutAuth_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", Role.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

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
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

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
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
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

    @Test
    void refresh_withValidRefreshToken_rotatesAndReturnsNewRefreshToken() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        JsonNode login = postJson("/auth/login", new LoginRequest("user@example.com", "password123"), status().isOk());
        String oldRefreshToken = login.get("refreshToken").asText();
        assertNotNull(oldRefreshToken);

        JsonNode refreshed = postJson("/auth/refresh", new RefreshRequest(oldRefreshToken), status().isOk());
        String newRefreshToken = refreshed.get("refreshToken").asText();

        assertNotEquals(oldRefreshToken, newRefreshToken);
        assertTrue(refreshed.get("token").asText().length() > 10);
    }

    @Test
    void refresh_withAlreadyRotatedRefreshToken_returnsUnauthorized() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        JsonNode login = postJson("/auth/login", new LoginRequest("user@example.com", "password123"), status().isOk());
        String oldRefreshToken = login.get("refreshToken").asText();

        JsonNode refreshed = postJson("/auth/refresh", new RefreshRequest(oldRefreshToken), status().isOk());
        String newRefreshToken = refreshed.get("refreshToken").asText();

        // The old token was revoked by rotation.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(oldRefreshToken))))
                .andExpect(status().isUnauthorized());

        // The new token should still work.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(newRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void logout_revokesRefreshToken() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        JsonNode login = postJson("/auth/login", new LoginRequest("user@example.com", "password123"), status().isOk());
        String refreshToken = login.get("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest(refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode postJson(String path, Object body, ResultMatcher... matchers) throws Exception {
        var request = post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        var action = mockMvc.perform(request);
        for (ResultMatcher matcher : matchers) {
            action.andExpect(matcher);
        }

        MvcResult result = action.andReturn();
        String content = result.getResponse().getContentAsString();
        return content == null || content.isBlank() ? objectMapper.nullNode() : objectMapper.readTree(content);
    }
}


