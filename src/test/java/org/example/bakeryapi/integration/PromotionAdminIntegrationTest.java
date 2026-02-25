package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryRepository;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductRepository;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionAdminIntegrationTest {

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
    void createBuyXPayY_asAdmin_returnsCreated() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = buyXPayYRequestBody(product.getId(), LocalDate.now(), null, 3, 2);

        mockMvc.perform(post("/promotions/buy-x-pay-y")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BUY_X_PAY_Y"))
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.productName").value(product.getName()))
                .andExpect(jsonPath("$.buyQuantity").value(3))
                .andExpect(jsonPath("$.payQuantity").value(2));
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
    void createBuyXPayY_withInvalidQuantities_returnsBadRequest() throws Exception {
        Product product = createProduct();
        String adminToken = createToken(Role.ADMIN);
        Map<String, Object> request = buyXPayYRequestBody(product.getId(), LocalDate.now(), null, 2, 2);

        mockMvc.perform(post("/promotions/buy-x-pay-y")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("buyQuantity must be greater than payQuantity"));
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

    private Map<String, Object> buyXPayYRequestBody(Long productId, LocalDate startDate, LocalDate endDate, int buyQuantity, int payQuantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("description", "Buy X Pay Y");
        body.put("startDate", startDate.toString());
        body.put("endDate", endDate == null ? null : endDate.toString());
        body.put("productId", productId);
        body.put("buyQuantity", buyQuantity);
        body.put("payQuantity", payQuantity);
        return body;
    }

    private String createToken(Role role) {
        String email = role.name().toLowerCase() + "@example.com";
        User user = new User(email, passwordEncoder.encode("password123"), role);
        userRepository.save(user);
        return jwtProvider.generateToken(email, role.name());
    }
}
