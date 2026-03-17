package com.bakery.api.config;

import com.bakery.api.config.properties.CacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    // ADR: docs/adr/0004-caching-caffeine.md (local Caffeine cache is safe for single-instance deployments)
    public static final String CATEGORIES_BY_ID = "categoriesById";
    public static final String CATEGORIES_LIST = "categoriesList";
    public static final String USERS_BY_ID = "usersById";
    public static final String USERS_BY_EMAIL = "usersByEmail";
    public static final String PROMOTIONS_BY_ID = "promotionsById";
    public static final String PROMOTIONS_ADMIN_LIST = "promotionsAdminList";
    public static final String PRODUCTS_ACTIVE_BY_ID = "productsActiveById";
    public static final String PRODUCTS_ACTIVE_LIST = "productsActiveList";

    @Bean
    public CacheManager cacheManager(CacheProperties properties) {
        Duration ttl = properties.ttl() == null || properties.ttl().isNegative() || properties.ttl().isZero()
                ? Duration.ofMinutes(10)
                : properties.ttl();
        int maxSize = properties.maxSize() <= 0 ? 10_000 : properties.maxSize();

        CaffeineCacheManager manager = new CaffeineCacheManager(
                CATEGORIES_BY_ID,
                CATEGORIES_LIST,
                USERS_BY_ID,
                USERS_BY_EMAIL,
                PROMOTIONS_BY_ID,
                PROMOTIONS_ADMIN_LIST,
                PRODUCTS_ACTIVE_BY_ID,
                PRODUCTS_ACTIVE_LIST
        );
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl));
        return manager;
    }
}
