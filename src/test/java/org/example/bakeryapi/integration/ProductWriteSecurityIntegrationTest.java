package org.example.bakeryapi.integration;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.dto.request.ProductRequest;
import org.example.bakeryapi.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductWriteSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void create_asUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String userToken = createToken(Role.USER);
        ProductRequest request = new ProductRequest("Baguette", null, new BigDecimal("1.00"), 10, category.getId());

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asAdmin_createsProduct() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String adminToken = createToken(Role.ADMIN);
        ProductRequest request = new ProductRequest("Baguette", null, new BigDecimal("1.00"), 10, category.getId());

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Baguette"))
                .andExpect(jsonPath("$.categoryId").value(category.getId()));
    }

    @Test
    void update_asUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 10, category));
        String userToken = createToken(Role.USER);
        ProductRequest request = new ProductRequest("Updated", null, new BigDecimal("2.00"), 5, category.getId());

        mockMvc.perform(put("/products/" + product.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void disable_enable_asUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 10, category));
        String userToken = createToken(Role.USER);

        mockMvc.perform(patch("/products/" + product.getId() + "/disable")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/products/" + product.getId() + "/enable")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void disable_enable_asAdmin_returnsNoContent() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 10, category));
        String adminToken = createToken(Role.ADMIN);

        mockMvc.perform(patch("/products/" + product.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/products/" + product.getId() + "/enable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

