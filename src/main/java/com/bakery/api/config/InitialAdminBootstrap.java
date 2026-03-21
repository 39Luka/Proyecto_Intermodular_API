package com.bakery.api.config;

import com.bakery.api.config.properties.InitialAdminProperties;
import com.bakery.api.user.UserRepository;
import com.bakery.api.user.UserService;
import com.bakery.api.user.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Bootstraps the very first admin user for a fresh database.
 *
 * Runs only when BOTH INITIAL_ADMIN_EMAIL and INITIAL_ADMIN_PASSWORD are set AND there are no users yet.
 *
 * ADR: docs/adr/0006-initial-admin-bootstrap.md
 */
@Component
public class InitialAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminBootstrap.class);

    private final InitialAdminProperties properties;
    private final UserRepository userRepository;
    private final UserService userService;

    public InitialAdminBootstrap(InitialAdminProperties properties, UserRepository userRepository, UserService userService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = trimToNull(properties.email());
        String password = trimToNull(properties.password());

        if (email == null && password == null) {
            return;
        }
        if (email == null || password == null) {
            throw new IllegalStateException(
                    "To bootstrap an initial admin user, set BOTH INITIAL_ADMIN_EMAIL and INITIAL_ADMIN_PASSWORD."
            );
        }

        if (userRepository.count() > 0) {
            log.info("Skipping initial admin bootstrap: users already exist.");
            return;
        }

        userService.createInternal(email, password, Role.ADMIN);
        log.info("Bootstrapped initial admin user: {}", email);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
