package com.bakery.bakeryapi.integration;

import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.product.Product;
import com.bakery.bakeryapi.userss.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicCatalogIntegrationTest extends AbstractIntegrationTest {

    @Test
    void listProducts_withoutToken_returnsOk_andOnlyActive() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));

        Product active = productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 10, category));

        Product inactive = new Product("Old bread", null, new BigDecimal("0.50"), 10, category);
        inactive.disable();
        productRepository.save(inactive);

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(active.getId()))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void getProductById_inactive_withoutToken_returnsNotFound() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product inactive = new Product("Old bread", null, new BigDecimal("0.50"), 10, category);
        inactive.disable();
        inactive = productRepository.save(inactive);

        mockMvc.perform(get("/products/" + inactive.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductById_inactive_asAdmin_returnsOk() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product inactive = new Product("Old bread", null, new BigDecimal("0.50"), 10, category);
        inactive.disable();
        inactive = productRepository.save(inactive);

        String adminToken = createToken(Role.ADMIN);
        mockMvc.perform(get("/products/" + inactive.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inactive.getId()))
                .andExpect(jsonPath("$.active").value(false));
    }
}

