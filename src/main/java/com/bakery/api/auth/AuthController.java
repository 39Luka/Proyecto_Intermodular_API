package com.bakery.api.auth;

import jakarta.validation.Valid;
import com.bakery.api.auth.dto.request.LoginRequest;
import com.bakery.api.auth.dto.request.LogoutRequest;
import com.bakery.api.auth.dto.request.RefreshRequest;
import com.bakery.api.auth.dto.request.RegisterRequest;
import com.bakery.api.auth.dto.response.LoginResponse;
import com.bakery.api.auth.refresh.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login, register, refresh and logout")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns an access token and refresh token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(
                request.email(),
                request.password()
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Creates a new USER account and returns tokens.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(
                        request.email(),
                        request.password()
                ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Rotates a refresh token and returns new tokens.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refreshed"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(
                request.refreshToken()
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes a refresh token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

}


