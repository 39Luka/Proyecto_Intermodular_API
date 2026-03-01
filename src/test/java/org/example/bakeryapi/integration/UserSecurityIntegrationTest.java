package org.example.bakeryapi.integration;

import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getUser_withoutToken_returnsUnauthorized() throws Exception {
        User target = userRepository.save(new User("target@example.com", passwordEncoder.encode("pass"), Role.USER));

        mockMvc.perform(get("/users/" + target.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUser_withUserToken_returnsForbidden() throws Exception {
        User target = userRepository.save(new User("target@example.com", passwordEncoder.encode("pass"), Role.USER));
        String token = createToken(Role.USER);

        mockMvc.perform(get("/users/" + target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_withAdminToken_returnsOk() throws Exception {
        User target = userRepository.save(new User("target@example.com", passwordEncoder.encode("pass"), Role.USER));
        String token = createToken(Role.ADMIN);

        mockMvc.perform(get("/users/" + target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("target@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}


