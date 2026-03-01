package org.example.bakeryapi.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.bakeryapi.auth.dto.request.LoginRequest;
import org.example.bakeryapi.auth.dto.request.LogoutRequest;
import org.example.bakeryapi.auth.dto.request.RefreshRequest;
import org.example.bakeryapi.auth.dto.request.RegisterRequest;
import org.example.bakeryapi.auth.dto.response.LoginResponse;
import org.example.bakeryapi.auth.dto.response.SessionResponse;
import org.example.bakeryapi.auth.refresh.RefreshTokenService;
import org.example.bakeryapi.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, UserService userService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
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
                        request.role(),
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

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> sessions(@AuthenticationPrincipal UserDetails principal) {
        var user = userService.getEntityByEmail(principal.getUsername());
        Instant now = Instant.now();

        List<SessionResponse> sessions = refreshTokenService.listForUser(user).stream()
                .filter(t -> !t.isRevoked())
                .filter(t -> !t.isExpired(now))
                .map(SessionResponse::from)
                .toList();

        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserDetails principal) {
        var user = userService.getEntityByEmail(principal.getUsername());
        refreshTokenService.revokeAllForUser(user);
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


