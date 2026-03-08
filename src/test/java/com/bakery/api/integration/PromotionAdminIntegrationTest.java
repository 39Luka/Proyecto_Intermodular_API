package com.bakery.api.integration;

import com.bakery.api.category.Category;
import com.bakery.api.product.Product;
import com.bakery.api.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PromotionAdminIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createPercentage_asUser_returnsForbidden() throws Exception {
        Product product = createProduct();
        String userToken = createToken(Role.USER);
        Map<String, Object> request = percentageRequestBody(product.getId(), LocalDate.now(), null, new BigDecimal("10.00"));

        mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPercentage_asAdmin_returnsCreated_withProductName() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = percentageRequestBody(product.getId(), LocalDate.now(), null, new BigDecimal("10.00"));

        mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.productName").value(product.getName()))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"));
    }

    @Test
    void createBuyXPayY_endpointIsNotAvailable() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);

        Map<String, Object> request = new HashMap<>();
        request.put("description", "Buy X Pay Y");
        request.put("startDate", LocalDate.now().toString());
        request.put("endDate", null);
        request.put("productId", product.getId());
        request.put("buyQuantity", 3);
        request.put("payQuantity", 2);

        mockMvc.perform(post("/promotions/buy-x-pay-y")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // The path is matched by "/promotions/{id}" (GET) but POST is not supported.
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void disable_enable_asUser_returnsForbidden() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = percentageRequestBody(product.getId(), LocalDate.now(), null, new BigDecimal("10.00"));

        String response = mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long promotionId = objectMapper.readTree(response).get("id").asLong();
        String userToken = createToken(Role.USER);

        mockMvc.perform(patch("/promotions/" + promotionId + "/disable")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/promotions/" + promotionId + "/enable")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPercentage_withStartDateInPast_returnsBadRequest() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = percentageRequestBody(product.getId(), LocalDate.now().minusDays(1), null, new BigDecimal("10.00"));

        mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Promotion start date cannot be in the past"));
    }

    @Test
    void getPromotionById_asUser_returnsForbidden_asAdmin_returnsOk() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = percentageRequestBody(product.getId(), LocalDate.now(), null, new BigDecimal("10.00"));

        String response = mockMvc.perform(post("/promotions/percentage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long promotionId = objectMapper.readTree(response).get("id").asLong();

        String userToken = createToken(Role.USER);
        mockMvc.perform(get("/promotions/" + promotionId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/promotions/" + promotionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promotionId))
                .andExpect(jsonPath("$.productName").value(product.getName()));
    }

    private Product createProduct() {
        Category category = categoryRepository.save(new Category("Bread"));
        return productRepository.save(new Product("Baguette", null, new BigDecimal("1.00"), 50, category));
    }

    private Map<String, Object> percentageRequestBody(Long productId, LocalDate startDate, LocalDate endDate, BigDecimal discountPercentage) {
        Map<String, Object> body = new HashMap<>();
        body.put("description", "10% OFF");
        body.put("startDate", startDate.toString());
        body.put("endDate", endDate == null ? null : endDate.toString());
        body.put("productId", productId);
        body.put("discountPercentage", discountPercentage);
        return body;
    }
}
