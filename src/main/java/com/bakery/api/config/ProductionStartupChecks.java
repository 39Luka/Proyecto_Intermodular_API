package com.bakery.api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Fail-fast checks for production.
 *
 * This prevents "it booted locally" configurations from reaching production with insecure defaults
 * (missing DB credentials, missing JWT secret, or accidental schema auto-update).
 */
@Component
@Profile("prod")
public class ProductionStartupChecks implements ApplicationRunner {

    private final Environment environment;

    public ProductionStartupChecks(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        requireNonBlank("DB_URL");
        requireNonBlank("DB_USERNAME");
        requireNonBlank("DB_PASSWORD");
        requireNonBlank("JWT_SECRET");

        // Never allow implicit schema mutations in prod. Recommended path: Flyway migrations + ddl-auto=validate.
        String ddlAuto = environment.getProperty("HIBERNATE_DDL_AUTO");
        if (ddlAuto == null || ddlAuto.isBlank()) {
            ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto", "validate");
        }
        ddlAuto = ddlAuto.toLowerCase(Locale.ROOT).trim();

        if (ddlAuto.equals("update") || ddlAuto.equals("create") || ddlAuto.equals("create-drop")) {
            throw new IllegalStateException(
                    "Unsafe HIBERNATE_DDL_AUTO=" + ddlAuto + " in production. " +
                    "Use Flyway migrations and keep HIBERNATE_DDL_AUTO=validate."
            );
        }
    }

    private void requireNonBlank(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required production environment variable: " + key);
        }
    }
}
