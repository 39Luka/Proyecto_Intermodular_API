package com.bakery.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.bakery.api.auth.dto.request.LoginRequest;
import com.bakery.api.auth.dto.request.LogoutRequest;
import com.bakery.api.auth.dto.request.RefreshRequest;
import com.bakery.api.auth.dto.request.RegisterRequest;
import com.bakery.api.auth.dto.response.LoginResponse;
import com.bakery.api.auth.refresh.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.login(
                request.email(),
                request.password(),
                clientIp(httpRequest),
                userAgent(httpRequest)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(
                        request.email(),
                        request.password(),
                        clientIp(httpRequest),
                        userAgent(httpRequest)
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.refresh(
                request.refreshToken(),
                clientIp(httpRequest),
                userAgent(httpRequest)
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private static String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // "client, proxy1, proxy2" -> take the original client.
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}


