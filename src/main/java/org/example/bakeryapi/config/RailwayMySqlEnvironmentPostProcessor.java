package org.example.bakeryapi.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class RailwayMySqlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String currentDatasourceUrl = environment.getProperty("spring.datasource.url");
        if (currentDatasourceUrl != null && currentDatasourceUrl.startsWith("jdbc:")) {
            return;
        }

        String rawUrl = firstNonBlank(
                environment.getProperty("DB_URL"),
                environment.getProperty("MYSQL_URL"),
                environment.getProperty("MYSQL_PUBLIC_URL")
        );

        if (rawUrl == null || rawUrl.startsWith("jdbc:") || !rawUrl.startsWith("mysql://")) {
            return;
        }

        Map<String, Object> overrides = new HashMap<>();
        applyFromMySqlUrl(rawUrl, overrides, environment);

        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("railwayMySqlOverrides", overrides));
        }
    }

    private static void applyFromMySqlUrl(String mysqlUrl, Map<String, Object> overrides, ConfigurableEnvironment environment) {
        try {
            URI uri = new URI(mysqlUrl);
            if (!"mysql".equalsIgnoreCase(uri.getScheme())) {
                return;
            }

            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String database = uri.getPath();
            if (database != null && database.startsWith("/")) {
                database = database.substring(1);
            }

            if (host != null && !host.isBlank() && database != null && !database.isBlank()) {
                overrides.put("spring.datasource.url", buildJdbcUrl(host, port, database));
            }

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                String username = parts.length > 0 ? decode(parts[0]) : null;
                String password = parts.length == 2 ? decode(parts[1]) : null;

                if (username != null && !username.isBlank() && isBlank(environment.getProperty("spring.datasource.username"))) {
                    overrides.put("spring.datasource.username", username);
                }
                if (password != null && !password.isBlank() && isBlank(environment.getProperty("spring.datasource.password"))) {
                    overrides.put("spring.datasource.password", password);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String buildJdbcUrl(String host, int port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
