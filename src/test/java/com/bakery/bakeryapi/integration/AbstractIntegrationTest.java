package com.bakery.bakeryapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.bakeryapi.catalog.category.CategoryRepository;
import com.bakery.bakeryapi.auth.refresh.RefreshTokenRepository;
import com.bakery.bakeryapi.catalog.product.ProductRepository;
import com.bakery.bakeryapi.promotionss.PromotionRepository;
import com.bakery.bakeryapi.promotionss.PromotionUsageRepository;
import com.bakery.bakeryapi.purchasess.PurchaseRepository;
import com.bakery.bakeryapi.security.JwtTokenService;
import com.bakery.bakeryapi.userss.UserRepository;
import com.bakery.bakeryapi.userss.domain.Role;
import com.bakery.bakeryapi.userss.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
// ADR: docs/adr/0010-testing-h2-and-mysql-testcontainers.md (MySQL ITs run only when Docker is available)
abstract class AbstractIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bakery_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        // Use real MySQL schema via Flyway migrations for integration tests.
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");

        // Make sure app-level placeholders don't interfere in tests.
        registry.add("DB_URL", mysql::getJdbcUrl);
        registry.add("DB_USERNAME", mysql::getUsername);
        registry.add("DB_PASSWORD", mysql::getPassword);
    }

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
    protected JwtTokenService jwtTokenService;

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
        return jwtTokenService.generateToken(email, role.name());
    }
}
