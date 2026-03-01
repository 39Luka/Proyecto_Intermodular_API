package org.example.bakeryapi.config;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryRepository;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductRepository;
import org.example.bakeryapi.user.UserRepository;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DefaultDataSeeder implements ApplicationRunner {

    /**
     * Seeds minimal default data when the database is empty.
     *
     * This is meant to reduce manual setup in new environments, while remaining safe:
     * - Only runs outside the "test" profile.
     * - Only inserts catalog data when both categories and products are empty.
     * - Only bootstraps an admin user when there are no users.
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultDataSeeder.class);

    private final Environment environment;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DefaultDataSeeder(
            Environment environment,
            UserRepository userRepository,
            UserService userService,
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminIfEmpty();

        // Only seed catalog when the database is effectively empty to avoid polluting a real dataset.
        if (categoryRepository.count() != 0 || productRepository.count() != 0) {
            return;
        }

        Map<String, Category> categories = seedCategories();
        seedProducts(categories);
    }

    private void seedAdminIfEmpty() {
        if (userRepository.count() != 0) {
            return;
        }

        String email = environment.getProperty("INITIAL_ADMIN_EMAIL");
        String password = environment.getProperty("INITIAL_ADMIN_PASSWORD");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("No users exist, but INITIAL_ADMIN_EMAIL/INITIAL_ADMIN_PASSWORD are not set; skipping admin bootstrap.");
            return;
        }

        userService.createInternal(email.trim(), password, Role.ADMIN);
        log.info("Bootstrapped initial ADMIN user: {}", email.trim());
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> categories = new LinkedHashMap<>();
        categories.put("Pan", categoryRepository.save(new Category("Pan")));
        categories.put("Bolleria", categoryRepository.save(new Category("Bolleria")));
        categories.put("Tartas", categoryRepository.save(new Category("Tartas")));
        categories.put("Galletas", categoryRepository.save(new Category("Galletas")));
        categories.put("Salado", categoryRepository.save(new Category("Salado")));
        categories.put("Bebidas", categoryRepository.save(new Category("Bebidas")));
        return categories;
    }

    private void seedProducts(Map<String, Category> categories) {
        productRepository.save(new Product(
                "Barra de pan",
                "Pan tradicional del dia",
                new BigDecimal("1.20"),
                50,
                categories.get("Pan")
        ));
        productRepository.save(new Product(
                "Croissant",
                "Croissant de mantequilla",
                new BigDecimal("1.50"),
                40,
                categories.get("Bolleria")
        ));
        productRepository.save(new Product(
                "Napolitana de chocolate",
                "Hojaldre relleno de chocolate",
                new BigDecimal("1.80"),
                35,
                categories.get("Bolleria")
        ));
        productRepository.save(new Product(
                "Tarta de queso",
                "Porcion de tarta de queso",
                new BigDecimal("3.50"),
                15,
                categories.get("Tartas")
        ));
        productRepository.save(new Product(
                "Cookie",
                "Galleta con pepitas de chocolate",
                new BigDecimal("1.00"),
                60,
                categories.get("Galletas")
        ));
        productRepository.save(new Product(
                "Empanada de atun",
                "Empanada individual",
                new BigDecimal("2.20"),
                25,
                categories.get("Salado")
        ));
        productRepository.save(new Product(
                "Cafe",
                "Cafe solo",
                new BigDecimal("1.30"),
                100,
                categories.get("Bebidas")
        ));
    }
}
