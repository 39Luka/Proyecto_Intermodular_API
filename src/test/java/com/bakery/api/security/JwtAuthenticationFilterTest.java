package com.bakery.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private HandlerExceptionResolver exceptionResolver;
    private JwtProvider jwtProvider;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        exceptionResolver = mock(HandlerExceptionResolver.class);
        jwtProvider = mock(JwtProvider.class);
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(exceptionResolver, jwtProvider, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_validToken_setsAuthenticationAndContinues() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");

        when(jwtProvider.getEmailFromToken("validtoken")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(org.springframework.security.core.userdetails.User.withUsername("user@example.com")
                        .password("hashed")
                        .roles("USER")
                        .build());

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(exceptionResolver);
    }

    @Test
    void doFilter_missingToken_skipsAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(exceptionResolver);
    }

    @Test
    void doFilter_invalidToken_clearsContextAndContinues() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");

        when(jwtProvider.getEmailFromToken("invalidtoken")).thenThrow(new JwtException("invalid token"));

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(), any(JwtException.class));
    }
}


