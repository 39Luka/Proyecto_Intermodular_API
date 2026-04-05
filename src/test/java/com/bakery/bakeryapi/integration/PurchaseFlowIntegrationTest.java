package com.bakery.bakeryapi.integration;

import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.product.Product;
import com.bakery.bakeryapi.promotionss.PercentagePromotion;
import com.bakery.bakeryapi.purchasess.dto.PurchaseItemRequest;
import com.bakery.bakeryapi.purchasess.dto.PurchaseRequest;
import com.bakery.bakeryapi.userss.domain.Role;
import com.bakery.bakeryapi.userss.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void create_thenCancel_restoresStock_andReleasesPromotionUsage() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                10,
                category
        ));
        PercentagePromotion promotion = new PercentagePromotion(
                "10% OFF",
                new BigDecimal("10.00"),
                LocalDate.now(),
                null,
                product
        );
        promotion = (PercentagePromotion) promotionRepository.save(promotion);

        User user = userRepository.save(new User("user@example.com", passwordEncoder.encode("password123"), Role.USER));
        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        PurchaseRequest request = new PurchaseRequest(
                user.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 2, promotion.getId()))
        );

        String purchaseResponse = mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].promotionId").value(promotion.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long purchaseId = objectMapper.readTree(purchaseResponse).get("id").asLong();

        Product afterCreate = productRepository.findById(product.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(8, afterCreate.getStock());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(patch("/purchases/" + purchaseId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Product afterCancel = productRepository.findById(product.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(10, afterCancel.getStock());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", product.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void pay_thenCancel_returnsBadRequest() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                10,
                category
        ));

        User user = userRepository.save(new User("user@example.com", passwordEncoder.encode("password123"), Role.USER));
        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());

        PurchaseRequest request = new PurchaseRequest(
                user.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 1, null))
        );

        String purchaseResponse = mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long purchaseId = objectMapper.readTree(purchaseResponse).get("id").asLong();

        mockMvc.perform(patch("/purchases/" + purchaseId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/purchases/" + purchaseId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_otherUsersPurchase_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                10,
                category
        ));

        User owner = userRepository.save(new User("owner@example.com", passwordEncoder.encode("password123"), Role.USER));
        User other = userRepository.save(new User("other@example.com", passwordEncoder.encode("password123"), Role.USER));
        String otherToken = jwtTokenService.generateToken(other.getEmail(), other.getRole().name());

        PurchaseRequest request = new PurchaseRequest(
                owner.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 1, null))
        );

        String ownerToken = jwtTokenService.generateToken(owner.getEmail(), owner.getRole().name());
        String purchaseResponse = mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long purchaseId = objectMapper.readTree(purchaseResponse).get("id").asLong();

        mockMvc.perform(patch("/purchases/" + purchaseId + "/pay")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }
}

