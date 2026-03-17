package com.bakery.api.config;

import com.bakery.api.config.properties.AppDataSourceProperties;
import com.bakery.api.config.properties.JpaHibernateProperties;
import com.bakery.api.config.properties.JwtProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Fail-fast checks for production.
 *
 * This prevents "it booted locally" configurations from reaching production with insecure defaults
 * (missing DB credentials, missing JWT secret, or accidental schema auto-update).
 *
 * Only active in the {@code prod} profile.
 *
 * ADR: docs/adr/0009-configuration-properties-and-prod-checks.md
 */
@Component
@Profile("prod")
public class ProductionStartupChecks implements ApplicationRunner {

    private final JpaHibernateProperties properties;
    private final AppDataSourceProperties dataSourceProperties;
    private final JwtProperties jwtProperties;

    public ProductionStartupChecks(
            JpaHibernateProperties properties,
            AppDataSourceProperties dataSourceProperties,
            JwtProperties jwtProperties
    ) {
        this.properties = properties;
        this.dataSourceProperties = dataSourceProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        requireNonBlank("spring.datasource.url", dataSourceProperties.url());
        requireNonBlank("spring.datasource.username", dataSourceProperties.username());
        requireNonBlank("spring.datasource.password", dataSourceProperties.password());
        requireNonBlank("jwt.secret", jwtProperties.secret());

        // Never allow implicit schema mutations in prod. Recommended path: Flyway migrations + ddl-auto=validate.
        // ADR: docs/adr/0012-schema-management-flyway.md
        String ddlAuto = properties.ddlAuto();
        ddlAuto = ddlAuto == null || ddlAuto.isBlank() ? "validate" : ddlAuto;
        ddlAuto = ddlAuto.toLowerCase(Locale.ROOT).trim();

        if (ddlAuto.equals("update") || ddlAuto.equals("create") || ddlAuto.equals("create-drop")) {
            throw new IllegalStateException(
                    "Unsafe HIBERNATE_DDL_AUTO=" + ddlAuto + " in production. " +
                    "Use Flyway migrations and keep HIBERNATE_DDL_AUTO=validate."
            );
        }
    }

    private static void requireNonBlank(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required production configuration: " + key);
        }
    }
}
