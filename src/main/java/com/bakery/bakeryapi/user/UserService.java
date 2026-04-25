package com.bakery.bakeryapi.user;

import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import com.bakery.bakeryapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getEntityById(Long id) {
        log.debug("Fetching user entity by ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    public User getEntityByEmail(String email) {
        log.debug("Fetching user entity by email: {}", email);
        return repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UserNotFoundException(email);
                });
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(getEntityById(id));
    }

    public UserResponse getByEmail(String email) {
        return UserResponse.from(getEntityByEmail(email));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        log.info("Creating new user with email: {}", request.email());
        User user = createInternal(request.email(), request.password(), request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public User createInternal(String email, String password, Role role) {
        if (repository.existsByEmail(email)) {
            log.warn("User creation failed: email already exists: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, role);
        User saved = repository.save(user);
        log.info("User created successfully with email: {}, role: {}", email, role);
        return saved;
    }

    @Transactional
    public User rotateRefreshToken(User user) {
        user.rotateRefreshToken();
        return repository.save(user);
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", id, enabled);
        User user = getEntityById(id);
        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }
        repository.save(user);
        log.info("User {} enabled status updated", id);
    }
}



