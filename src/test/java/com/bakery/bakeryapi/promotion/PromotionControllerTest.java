package com.bakery.bakeryapi.promotion;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.promotion.dto.PercentagePromotionRequest;
import com.bakery.bakeryapi.promotion.dto.PromotionResponse;
import com.bakery.bakeryapi.repository.CategoryRepository;
import com.bakery.bakeryapi.repository.ProductRepository;
import com.bakery.bakeryapi.shared.dto.ActiveUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setup() {
        Category category = categoryRepository.save(new Category(uniqueName("Test Category")));
        Product product = productRepository.save(
                new Product("Test Product " + UUID.randomUUID(), "A test product", new BigDecimal("10.00"), 100, category)
        );
        productId = product.getId();
    }

    @Test
    void testGetActivePromotionsByProductIdSuccess() throws Exception {
        mockMvc.perform(get("/promotions/active")
                        .param("productId", productId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreatePercentagePromotionSuccess() throws Exception {
        PercentagePromotionRequest request = percentagePromotionRequest("Summer Sale");

        mockMvc.perform(post("/promotions/percentage")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(jwt -> jwt.subject("admin@example.com").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Summer Sale"));
    }

    @Test
    void testCreatePromotionForbiddenForUserRole() throws Exception {
        PercentagePromotionRequest request = percentagePromotionRequest("Summer Sale");

        mockMvc.perform(post("/promotions/percentage")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))
                                .jwt(jwt -> jwt.subject("user@example.com").claim("role", "USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreatePromotionUnauthorized() throws Exception {
        PercentagePromotionRequest request = percentagePromotionRequest("Summer Sale");

        mockMvc.perform(post("/promotions/percentage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPatchPromotionActiveSuccess() throws Exception {
        PromotionResponse created = createPromotion("Flash Sale");

        mockMvc.perform(patch("/promotions/" + created.id())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(jwt -> jwt.subject("admin@example.com").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ActiveUpdateRequest(false))))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPromotionByIdNotFound() throws Exception {
        mockMvc.perform(get("/promotions/99999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(jwt -> jwt.subject("admin@example.com").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private PromotionResponse createPromotion(String description) throws Exception {
        String response = mockMvc.perform(post("/promotions/percentage")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(jwt -> jwt.subject("admin@example.com").claim("role", "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(percentagePromotionRequest(description))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, PromotionResponse.class);
    }

    private PercentagePromotionRequest percentagePromotionRequest(String description) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        return new PercentagePromotionRequest(
                description,
                startDate,
                startDate.plusDays(7),
                productId,
                new BigDecimal("20.00")
        );
    }

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
