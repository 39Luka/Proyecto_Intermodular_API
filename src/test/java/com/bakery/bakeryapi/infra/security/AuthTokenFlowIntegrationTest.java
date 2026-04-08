package com.bakery.bakeryapi.infra.security;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.domain.PercentagePromotion;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.repository.CategoryRepository;
import com.bakery.bakeryapi.repository.ProductRepository;
import com.bakery.bakeryapi.repository.PromotionRepository;
import com.bakery.bakeryapi.repository.PromotionUsageRepository;
import com.bakery.bakeryapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthTokenFlowIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionUsageRepository promotionUsageRepository;

    @BeforeEach
    void cleanDb() {
        // Keep cleanup explicit because these tests call the app over HTTP (no test transaction rollback).
        promotionUsageRepository.deleteAll();
        promotionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void user_can_login_get_token_and_filter_active_promotions_for_self() throws Exception {
        String email = "user+" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        register(email, password);
        String token = loginAndGetToken(email, password);

        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        assertNotNull(userId);

        Product product = seedProductWithActivePromotion();

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void user_token_cannot_filter_active_promotions_for_other_user() throws Exception {
        String email = "user+" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        register(email, password);
        String token = loginAndGetToken(email, password);

        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        assertNotNull(userId);

        Product product = seedProductWithActivePromotion();

        long otherUserId = userId + 999;
        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .param("userId", String.valueOf(otherUserId))
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private void register(String email, String password) {
        try {
            MvcResult result = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
            assertNotNull(body.get("token"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String loginAndGetToken(String email, String password) {
        try {
            MvcResult result = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                    .andExpect(status().isOk())
                    .andReturn();

            Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
            Object token = body.get("token");
            assertNotNull(token);
            return token.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Product seedProductWithActivePromotion() {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product("Baguette", "Test", new BigDecimal("1.00"), 10, category));

        LocalDate today = LocalDate.now();
        promotionRepository.save(new PercentagePromotion(
                "Test promo",
                new BigDecimal("10.00"),
                today.minusDays(1),
                today.plusDays(1),
                product
        ));

        return product;
    }
}
