package com.bakery.bakeryapi.auth;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.auth.dto.LoginRequest;
import com.bakery.bakeryapi.auth.dto.RefreshTokenRequest;
import com.bakery.bakeryapi.auth.dto.RegisterRequest;
import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login, register, and refresh JWT tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns access and refresh tokens.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
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
    @Operation(summary = "Register", description = "Creates a new USER account and returns access and refresh tokens.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
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
    @Operation(summary = "Refresh token", description = "Uses a refresh token to obtain a new access token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token generated"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken()));
    }

}


