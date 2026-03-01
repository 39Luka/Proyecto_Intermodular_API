package org.example.bakeryapi.integration;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.purchase.domain.Purchase;
import org.example.bakeryapi.purchase.domain.PurchaseItem;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.example.bakeryapi.purchase.dto.request.PurchaseItemRequest;
import org.example.bakeryapi.purchase.dto.request.PurchaseRequest;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseAccessIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getPurchase_otherUsersPurchase_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                50,
                category
        ));

        User owner = createUser(Role.USER, "owner@example.com");
        User other = createUser(Role.USER, "other@example.com");
        String otherToken = jwtProvider.generateToken(other.getEmail(), other.getRole().name());

        Purchase purchase = new Purchase(owner, LocalDateTime.now(), PurchaseStatus.CREATED);
        purchase.addItem(new PurchaseItem(
                product,
                null,
                1,
                product.getPrice(),
                BigDecimal.ZERO,
                product.getPrice()
        ));
        purchase = purchaseRepository.save(purchase);

        mockMvc.perform(get("/purchases/" + purchase.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPurchase_forAnotherUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                50,
                category
        ));

        User owner = createUser(Role.USER, "owner@example.com");
        User other = createUser(Role.USER, "other@example.com");
        String otherToken = jwtProvider.generateToken(other.getEmail(), other.getRole().name());

        PurchaseRequest request = new PurchaseRequest(
                owner.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 1, null))
        );

        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cannot create a purchase for another user"));
    }

    @Test
    void getAllPurchases_asUser_ignoresUserIdParam() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                50,
                category
        ));

        User owner = createUser(Role.USER, "owner@example.com");
        User other = createUser(Role.USER, "other@example.com");
        String otherToken = jwtProvider.generateToken(other.getEmail(), other.getRole().name());

        Purchase purchase = new Purchase(owner, LocalDateTime.now(), PurchaseStatus.CREATED);
        purchase.addItem(new PurchaseItem(
                product,
                null,
                1,
                product.getPrice(),
                BigDecimal.ZERO,
                product.getPrice()
        ));
        purchaseRepository.save(purchase);

        mockMvc.perform(get("/purchases")
                        .param("userId", owner.getId().toString())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getAllPurchases_asAdmin_canFilterByUserId() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                50,
                category
        ));

        User owner = createUser(Role.USER, "owner@example.com");
        User admin = createUser(Role.ADMIN, "admin@example.com");
        String adminToken = jwtProvider.generateToken(admin.getEmail(), admin.getRole().name());

        Purchase purchase = new Purchase(owner, LocalDateTime.now(), PurchaseStatus.CREATED);
        purchase.addItem(new PurchaseItem(
                product,
                null,
                1,
                product.getPrice(),
                BigDecimal.ZERO,
                product.getPrice()
        ));
        purchase = purchaseRepository.save(purchase);

        mockMvc.perform(get("/purchases")
                        .param("userId", owner.getId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(purchase.getId()))
                .andExpect(jsonPath("$.content[0].userId").value(owner.getId()));
    }

    private User createUser(Role role, String email) {
        return userRepository.save(new User(email, passwordEncoder.encode("password123"), role));
    }
}
