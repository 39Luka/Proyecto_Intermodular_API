package com.bakery.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Ensure java.time types (Instant, etc.) serialize correctly in filter-generated responses.
        return new ObjectMapper().findAndRegisterModules();
    }
}

