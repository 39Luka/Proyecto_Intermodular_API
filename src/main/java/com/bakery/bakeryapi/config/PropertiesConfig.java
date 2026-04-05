package com.bakery.bakeryapi.config;

import com.bakery.bakeryapi.config.properties.CorsProperties;
import com.bakery.bakeryapi.config.properties.InitialAdminProperties;
import com.bakery.bakeryapi.config.properties.JpaHibernateProperties;
import com.bakery.bakeryapi.config.properties.JwtProperties;
import com.bakery.bakeryapi.config.properties.AppDataSourceProperties;
import com.bakery.bakeryapi.config.properties.RateLimitProperties;
import com.bakery.bakeryapi.config.properties.RefreshTokenProperties;
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
        AppDataSourceProperties.class
})
public class PropertiesConfig {
}
