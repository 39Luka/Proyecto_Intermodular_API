package com.bakery.bakeryapi.user;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.shared.dto.ActiveUpdateRequest;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }


    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse user = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> patchUser(@PathVariable Long id, @RequestBody @Valid ActiveUpdateRequest request) {
        // API uses a unified flag name: { "active": true|false }. For users it maps to the "enabled" state.
        service.setEnabled(id, request.active());
        return ResponseEntity.noContent().build();
    }

}
