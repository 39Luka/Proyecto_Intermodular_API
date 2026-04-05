package com.bakery.bakeryapi.security;

import org.springframework.beans.factory.annotation.Qualifier;
import com.bakery.bakeryapi.config.properties.CorsProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    /**
     * HTTP security rules:
     * - /auth/** and Swagger are public
     * - everything else requires authentication
     *
     * Fine-grained authorization is enforced via method security (@PreAuthorize).
     */
    private final HandlerExceptionResolver exceptionResolver;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            CorsProperties corsProperties
    ) {
        this.exceptionResolver = exceptionResolver;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RateLimitingFilter rateLimitingFilter,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh", "/auth/logout").permitAll()
                        .requestMatchers("/auth/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // ADR: docs/adr/0004-observability-actuator-prometheus.md (public health + Prometheus scraping)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        // Public read-only endpoints (catalog + active promotions).
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/promotions/active").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // ADR: docs/adr/0002-jwt-validation-resource-server-hs256.md
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(request, response, null, authException)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(request, response, null, accessDeniedException)
                        )
                )
                // Authenticate first so rate limiting can key off the user identity when available.
                .addFilterAfter(rateLimitingFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Comma-separated list, e.g.: https://myapp.com,http://localhost:5173
        // When empty, CORS is effectively disabled (no allowed origins).
        String raw = corsProperties.allowedOrigins() == null ? "" : corsProperties.allowedOrigins();
        List<String> origins = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);

        if (origins.size() == 1 && origins.get(0).equals("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
