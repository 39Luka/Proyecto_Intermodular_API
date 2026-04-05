package com.bakery.bakeryapi.integration;

import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.product.Product;
import com.bakery.bakeryapi.userss.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicPromotionsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void listActivePromotions_withoutToken_returnsOk() throws Exception {
        Product product = createProduct();
        Long promotionId = createPercentagePromotion(product.getId());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void listActivePromotions_withoutToken_withUserId_returnsUnauthorized() throws Exception {
        Product product = createProduct();
        createPercentagePromotion(product.getId());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private Product createProduct() {
        Category category = categoryRepository.save(new Category("Bread"));
        return productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 50, category));
    }

    private Long createPercentagePromotion(Long productId) throws Exception {
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> body = new HashMap<>();
        body.put("description", "10% OFF");
        body.put("startDate", LocalDate.now().toString());
        body.put("endDate", null);
        body.put("productId", productId);
        body.put("discountPercentage", new BigDecimal("10.00"));

        String response = mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}

