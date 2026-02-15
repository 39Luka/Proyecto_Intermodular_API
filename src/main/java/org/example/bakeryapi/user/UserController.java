package org.example.bakeryapi.user;

import jakarta.validation.Valid;
import org.example.bakeryapi.user.dto.UserRequest;
import org.example.bakeryapi.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = service.getById(id);
        return UserResponse.from(user);
    }

    @GetMapping
    public UserResponse getByEmail(@RequestParam String email) {
        User user = service.getByEmail(email);
        return UserResponse.from(user);
    }


    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        User user = service.create(request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }


    @PatchMapping("/{id}/disable")
    public void disableUser(@PathVariable Long id) {
        service.disableUser(id);
    }

    @PatchMapping("/{id}/enable")
    public void enableUser(@PathVariable Long id) {
        service.enableUser(id);
    }

}
