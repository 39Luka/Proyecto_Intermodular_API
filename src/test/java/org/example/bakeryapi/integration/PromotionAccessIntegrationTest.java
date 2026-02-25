package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryRepository;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductRepository;
import org.example.bakeryapi.promotion.PromotionRepository;
import org.example.bakeryapi.promotion.PromotionUsageRepository;
import org.example.bakeryapi.promotion.domain.PercentagePromotion;
import org.example.bakeryapi.purchase.PurchaseRepository;
import org.example.bakeryapi.purchase.dto.PurchaseItemRequest;
import org.example.bakeryapi.purchase.dto.PurchaseRequest;
import org.example.bakeryapi.security.JwtProvider;
import org.example.bakeryapi.user.UserRepository;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionUsageRepository promotionUsageRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        promotionUsageRepository.deleteAll();
        promotionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        objectMapper = new ObjectMapper();
    }

    @Test
    void promotionsList_asUser_returnsForbidden_butActiveEndpointWorks() throws Exception {
        PromotionFixture fixture = createPromotionFixture();
        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/promotions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(fixture.product().getId()))
                .andExpect(jsonPath("$.content[0].productName").value(fixture.product().getName()));
    }

    @Test
    void activePromotions_afterUsingPromotion_disappearsForThatUser() throws Exception {
        PromotionFixture fixture = createPromotionFixture();
        User user = createUser(Role.USER, "user@example.com");
        String userToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                user.getId(),
                List.of(new PurchaseItemRequest(fixture.product().getId(), 2, fixture.promotionId()))
        );

        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].promotionId").value(fixture.promotionId()));

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void activePromotions_userCannotQueryAnotherUsersPromotions() throws Exception {
        PromotionFixture fixture = createPromotionFixture();
        User user = createUser(Role.USER, "user@example.com");
        User other = createUser(Role.USER, "other@example.com");
        String userToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .param("userId", other.getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void activePromotions_adminCanFilterByUserId_orSeeAllWhenNull() throws Exception {
        PromotionFixture fixture = createPromotionFixture();
        User user = createUser(Role.USER, "user@example.com");
        User admin = createUser(Role.ADMIN, "admin@example.com");
        String userToken = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String adminToken = jwtProvider.generateToken(admin.getEmail(), admin.getRole().name());

        PurchaseRequest purchaseRequest = new PurchaseRequest(
                user.getId(),
                List.of(new PurchaseItemRequest(fixture.product().getId(), 2, fixture.promotionId()))
        );
        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .param("userId", user.getId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/promotions/active")
                        .param("productId", fixture.product().getId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private PromotionFixture createPromotionFixture() {
        Category category = categoryRepository.save(new Category("Bread"));
        Product product = productRepository.save(new Product(
                "Baguette",
                null,
                new BigDecimal("1.00"),
                50,
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

        return new PromotionFixture(product, promotion.getId());
    }

    private String createToken(Role role) {
        User user = createUser(role, role.name().toLowerCase() + "@example.com");
        return jwtProvider.generateToken(user.getEmail(), user.getRole().name());
    }

    private User createUser(Role role, String email) {
        return userRepository.save(new User(email, passwordEncoder.encode("password123"), role));
    }

    private record PromotionFixture(Product product, Long promotionId) {
    }
}

