package org.example.bakeryapi.config;

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

        // Never allow implicit schema mutations in prod unless explicitly opted-in for a one-time bootstrap.
        // Recommended path: Flyway migrations + ddl-auto=validate.
        String ddlAuto = environment.getProperty("HIBERNATE_DDL_AUTO", "validate").toLowerCase(Locale.ROOT).trim();
        boolean allowUnsafe = Boolean.parseBoolean(environment.getProperty("APP_ALLOW_UNSAFE_DDL_AUTO", "false"));

        if (!allowUnsafe && (ddlAuto.equals("update") || ddlAuto.equals("create") || ddlAuto.equals("create-drop"))) {
            throw new IllegalStateException(
                    "Unsafe HIBERNATE_DDL_AUTO=" + ddlAuto + " in production. " +
                    "Use Flyway migrations and keep HIBERNATE_DDL_AUTO=validate. " +
                    "If you must bootstrap temporarily, set APP_ALLOW_UNSAFE_DDL_AUTO=true."
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

