package org.example.bakeryapi.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.example.bakeryapi.user.UserRepository;
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
     * - /users/** is ADMIN-only
     * - write operations on categories/products/promotions are ADMIN-only
     * - everything else requires authentication
     */
    private final HandlerExceptionResolver exceptionResolver;
    private final Environment environment;

    public SecurityConfig(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            Environment environment
    ) {
        this.exceptionResolver = exceptionResolver;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RateLimitingFilter rateLimitingFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/promotions/active").authenticated()
                        .requestMatchers(HttpMethod.GET, "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/**", "/categories/**", "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**", "/categories/**", "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/**", "/categories/**", "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**", "/categories/**", "/promotions/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(request, response, null, authException)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(request, response, null, accessDeniedException)
                        )
                )
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitingFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Comma-separated list, e.g.: https://myapp.com,http://localhost:5173
        // When empty, CORS is effectively disabled (no allowed origins).
        String raw = environment.getProperty("app.cors.allowed-origins", "");
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
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .disabled(!user.isEnabled())
                        .roles(user.getRole().name())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
