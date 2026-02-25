package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryRepository;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductRepository;
import org.example.bakeryapi.product.dto.ProductRequest;
import org.example.bakeryapi.promotion.PromotionRepository;
import org.example.bakeryapi.promotion.PromotionUsageRepository;
import org.example.bakeryapi.purchase.PurchaseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductWriteSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PromotionUsageRepository promotionUsageRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    private String createToken(Role role) {
        String email = role.name().toLowerCase() + "@example.com";
        User user = new User(email, passwordEncoder.encode("password123"), role);
        userRepository.save(user);
        return jwtProvider.generateToken(email, role.name());
    }
}

