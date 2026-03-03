package com.bakery.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.api.category.CategoryRepository;
import com.bakery.api.auth.refresh.RefreshTokenRepository;
import com.bakery.api.product.ProductRepository;
import com.bakery.api.promotion.PromotionRepository;
import com.bakery.api.promotion.PromotionUsageRepository;
import com.bakery.api.purchase.PurchaseRepository;
import com.bakery.api.security.JwtProvider;
import com.bakery.api.user.UserRepository;
import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Autowired
    protected PurchaseRepository purchaseRepository;

    @Autowired
    protected PromotionUsageRepository promotionUsageRepository;

    @Autowired
    protected PromotionRepository promotionRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @Autowired
    protected JwtProvider jwtProvider;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Order matters due to FK relationships.
        purchaseRepository.deleteAll();
        promotionUsageRepository.deleteAll();
        promotionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String createToken(Role role) {
        String email = role.name().toLowerCase() + "@example.com";
        User user = new User(email, passwordEncoder.encode("password123"), role);
        userRepository.save(user);
        return jwtProvider.generateToken(email, role.name());
    }
}
