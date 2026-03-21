package com.bakery.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.bakery.api.auth.dto.LoginRequest;
import com.bakery.api.auth.dto.LogoutRequest;
import com.bakery.api.auth.dto.RefreshRequest;
import com.bakery.api.auth.dto.RegisterRequest;
import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
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
    void register_withoutAuth_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertTrue(userRepository.existsByEmail("user@example.com"));
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


