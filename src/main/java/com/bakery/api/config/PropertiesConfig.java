package com.bakery.api.config;

import com.bakery.api.config.properties.CorsProperties;
import com.bakery.api.config.properties.CacheProperties;
import com.bakery.api.config.properties.InitialAdminProperties;
import com.bakery.api.config.properties.JpaHibernateProperties;
import com.bakery.api.config.properties.JwtProperties;
import com.bakery.api.config.properties.AppDataSourceProperties;
import com.bakery.api.config.properties.RateLimitProperties;
import com.bakery.api.config.properties.RefreshTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        RateLimitProperties.class,
        RefreshTokenProperties.class,
        InitialAdminProperties.class,
        JpaHibernateProperties.class,
        CacheProperties.class,
        AppDataSourceProperties.class
})
public class PropertiesConfig {
}
