package org.example.bakeryapi.integration;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.dto.CategoryRequest;
import org.example.bakeryapi.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategorySecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getAll_asUser_returnsOk() throws Exception {
        categoryRepository.save(new Category("Bread"));
        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_asUser_returnsForbidden() throws Exception {
        String userToken = createToken(Role.USER);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asAdmin_createsCategory() throws Exception {
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bread"));
    }

    @Test
    void create_duplicateName_returnsConflict() throws Exception {
        categoryRepository.save(new Category("Bread"));
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_asUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String userToken = createToken(Role.USER);
        CategoryRequest request = new CategoryRequest("New name");

        mockMvc.perform(put("/categories/" + category.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_asAdmin_updatesCategory() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("New name");

        mockMvc.perform(put("/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("New name"));
    }
}

