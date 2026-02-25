package org.example.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryRepository;
import org.example.bakeryapi.category.dto.CategoryRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategorySecurityIntegrationTest {

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
    void getAll_asUser_returnsOk() throws Exception {
        categoryRepository.save(new Category("Bread"));
        String userToken = createToken(Role.USER);

        mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_asUser_returnsForbidden() throws Exception {
        String userToken = createToken(Role.USER);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asAdmin_createsCategory() throws Exception {
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bread"));
    }

    @Test
    void create_duplicateName_returnsConflict() throws Exception {
        categoryRepository.save(new Category("Bread"));
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("Bread");

        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_asUser_returnsForbidden() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String userToken = createToken(Role.USER);
        CategoryRequest request = new CategoryRequest("New name");

        mockMvc.perform(put("/categories/" + category.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_asAdmin_updatesCategory() throws Exception {
        Category category = categoryRepository.save(new Category("Bread"));
        String adminToken = createToken(Role.ADMIN);
        CategoryRequest request = new CategoryRequest("New name");

        mockMvc.perform(put("/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("New name"));
    }

    private String createToken(Role role) {
        String email = role.name().toLowerCase() + "@example.com";
        User user = new User(email, passwordEncoder.encode("password123"), role);
        userRepository.save(user);
        return jwtProvider.generateToken(email, role.name());
    }
}

