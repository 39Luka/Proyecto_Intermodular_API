package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.bakeryapi.auth.dto.request.LoginRequest;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSessionsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void sessions_and_logoutAll_workForCurrentUser() throws Exception {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), Role.USER);
        userRepository.save(user);

        JsonNode login1 = login("user@example.com", "password123");
        String accessToken = login1.get("token").asText();
        String refresh1 = login1.get("refreshToken").asText();

        JsonNode login2 = login("user@example.com", "password123");
        String refresh2 = login2.get("refreshToken").asText();

        mockMvc.perform(get("/auth/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(post("/auth/logout-all")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Both refresh tokens should now be invalid.
        assertTrue(isRefreshUnauthorized(refresh1));
        assertTrue(isRefreshUnauthorized(refresh2));

        mockMvc.perform(get("/auth/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private JsonNode login(String email, String password) throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private boolean isRefreshUnauthorized(String refreshToken) throws Exception {
        var result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();
        return result.getResponse().getStatus() == 401;
    }
}
