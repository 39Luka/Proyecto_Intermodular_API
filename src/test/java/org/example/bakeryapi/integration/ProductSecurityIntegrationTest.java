package org.example.bakeryapi.integration;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.purchase.domain.Purchase;
import org.example.bakeryapi.purchase.domain.PurchaseItem;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getAll_asUser_returnsOnlyActiveProducts() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));

        Product active = productRepository.save(new Product(
                "Baguette",
                "Fresh baguette",
                new BigDecimal("1.00"),
                10,
                category
        ));
        Product inactive = new Product(
                "Old bread",
                "Should not be visible",
                new BigDecimal("0.50"),
                10,
                category
        );
        inactive.disable();
        productRepository.save(inactive);

        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(active.getId()))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void getById_inactiveProduct_asUser_returnsNotFound() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product inactive = new Product(
                "Old bread",
                "Should not be visible",
                new BigDecimal("0.50"),
                10,
                category
        );
        inactive.disable();
        inactive = productRepository.save(inactive);

        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/products/" + inactive.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_inactiveProduct_asAdmin_returnsOk() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        Product inactive = new Product(
                "Old bread",
                "Visible for admin",
                new BigDecimal("0.50"),
                10,
                category
        );
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

    @Test
    void getAll_sizeIsCappedTo100() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        IntStream.range(0, 150).forEach(i -> productRepository.save(new Product(
                "Bread " + i,
                null,
                new BigDecimal("1.00"),
                10,
                category
        )));

        String adminToken = createToken(Role.ADMIN);

        mockMvc.perform(get("/products")
                        .param("size", "1000")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(100));
    }

    @Test
    void topSelling_asUser_excludesInactiveProducts() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));

        Product active = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                10,
                category
        ));

        Product inactive = new Product(
                "Old bread",
                null,
                new BigDecimal("0.50"),
                10,
                category
        );
        inactive.disable();
        inactive = productRepository.save(inactive);

        User buyer = userRepository.save(new User("buyer@example.com", passwordEncoder.encode("pass"), Role.USER));

        Purchase purchase = new Purchase(buyer, LocalDateTime.now(), PurchaseStatus.PAID);
        purchase.addItem(new PurchaseItem(
                active,
                null,
                10,
                active.getPrice(),
                BigDecimal.ZERO,
                active.getPrice().multiply(BigDecimal.TEN)
        ));
        purchase.addItem(new PurchaseItem(
                inactive,
                null,
                10,
                inactive.getPrice(),
                BigDecimal.ZERO,
                inactive.getPrice().multiply(BigDecimal.TEN)
        ));
        purchaseRepository.save(purchase);

        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/products/top-selling")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(active.getId()));
    }

    @Test
    void topSelling_asAdmin_includesInactiveProducts() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));

        Product active = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                10,
                category
        ));

        Product inactive = new Product(
                "Old bread",
                null,
                new BigDecimal("0.50"),
                10,
                category
        );
        inactive.disable();
        inactive = productRepository.save(inactive);

        User buyer = userRepository.save(new User("buyer@example.com", passwordEncoder.encode("pass"), Role.USER));

        Purchase purchase = new Purchase(buyer, LocalDateTime.now(), PurchaseStatus.PAID);
        purchase.addItem(new PurchaseItem(
                active,
                null,
                10,
                active.getPrice(),
                BigDecimal.ZERO,
                active.getPrice().multiply(BigDecimal.TEN)
        ));
        purchase.addItem(new PurchaseItem(
                inactive,
                null,
                10,
                inactive.getPrice(),
                BigDecimal.ZERO,
                inactive.getPrice().multiply(BigDecimal.TEN)
        ));
        purchaseRepository.save(purchase);

        String adminToken = createToken(Role.ADMIN);

        mockMvc.perform(get("/products/top-selling")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
}

