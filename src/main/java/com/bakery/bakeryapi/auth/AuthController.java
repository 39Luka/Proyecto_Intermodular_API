package com.bakery.bakeryapi.auth;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.auth.dto.LoginRequest;
import com.bakery.bakeryapi.auth.dto.RefreshTokenRequest;
import com.bakery.bakeryapi.auth.dto.RegisterRequest;
import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.user.dto.PasswordUpdateRequest;
import com.bakery.bakeryapi.user.dto.ProfileImageUpdateRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for authentication and authenticated account actions.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login, register, and refresh JWT tokens")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns access and refresh tokens.", security = {})
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
    @Operation(summary = "Register", description = "Creates a new USER account and returns access and refresh tokens.", security = {})
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
    @Operation(summary = "Refresh token", description = "Uses a refresh token to obtain a new access token.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token generated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken()));
    }

    /**
     * Returns the authenticated user's account data.
     *
     * @return current user data
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my user", description = "Returns the authenticated user's account data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getMe() {
        String email = SecurityUtils.requireAuthentication().getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    /**
     * Updates or removes the profile image of the authenticated user.
     *
     * @param request request containing the Base64 image, or an empty value to remove it
     * @return updated user data
     */
    @PatchMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my profile image", description = "Updates or removes the authenticated user's profile image.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid image"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> updateMyProfileImage(
            @RequestBody ProfileImageUpdateRequest request
    ) {
        String email = SecurityUtils.requireAuthentication().getName();
        return ResponseEntity.ok(userService.updateProfileImage(email, request.profileImageBase64()));
    }

    /**
     * Changes the password of the authenticated user after checking the current password.
     *
     * @param request request containing the current password and the new password
     * @return empty response when the password is changed
     */
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change my password", description = "Changes the authenticated user's password after validating the current password.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid current password or unauthorized")
    })
    public ResponseEntity<Void> changeMyPassword(
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        String email = SecurityUtils.requireAuthentication().getName();
        userService.changePassword(email, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

}


