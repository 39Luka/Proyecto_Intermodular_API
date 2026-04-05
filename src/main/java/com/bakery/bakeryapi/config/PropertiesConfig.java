package com.bakery.bakeryapi.config;

import com.bakery.bakeryapi.config.properties.CorsProperties;
import com.bakery.bakeryapi.config.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class
})
public class PropertiesConfig {
}
