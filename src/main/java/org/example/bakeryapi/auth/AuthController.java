package org.example.bakeryapi.auth;

import jakarta.validation.Valid;
import org.example.bakeryapi.auth.dto.LoginRequest;
import org.example.bakeryapi.auth.dto.LoginResponse;
import org.example.bakeryapi.auth.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String token = authService.login(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        String token = authService.register(
                request.email(),
                request.password(),
                request.role()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new LoginResponse(token));
    }

}
