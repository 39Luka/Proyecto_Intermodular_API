package com.bakery.bakeryapi.user;

import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import com.bakery.bakeryapi.repository.UserRepository;
import com.bakery.bakeryapi.shared.ImageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Application service for user management, profile image updates and password changes.
 */
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

    /**
     * Updates the profile image for the user identified by email.
     *
     * Blank or {@code null} image content removes the existing profile image. Non-empty
     * values must be valid Base64 images accepted by {@link ImageValidator}.
     *
     * @param email email of the user to update
     * @param profileImageBase64 image encoded as Base64, or {@code null}/blank to remove it
     * @return updated user response
     * @throws UserNotFoundException when no user exists for the provided email
     * @throws com.bakery.bakeryapi.shared.exception.InvalidImageException when the image is invalid
     */
    @Transactional
    public UserResponse updateProfileImage(String email, String profileImageBase64) {
        log.info("Updating profile image for user: {}", email);
        User user = getEntityByEmail(email);
        user.setProfileImage(decodeOptionalImage(profileImageBase64));
        User saved = repository.save(user);
        log.info("Profile image updated for user: {}", email);
        return UserResponse.from(saved);
    }

    /**
     * Changes the password for the user identified by email.
     *
     * The current password must match the stored password. After changing it, the refresh
     * token version is rotated so previous refresh tokens become invalid.
     *
     * @param email email of the user changing their password
     * @param currentPassword current raw password
     * @param newPassword new raw password to encode and persist
     * @throws UserNotFoundException when no user exists for the provided email
     * @throws InvalidCredentialsException when the current password does not match
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", email);
        User user = getEntityByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed: invalid current password for email: {}", email);
            throw new InvalidCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.rotateRefreshToken();
        repository.save(user);
        log.info("Password changed for user: {}", email);
    }

    /**
     * Converts an optional Base64 image into bytes after validation.
     *
     * @param imageBase64 image encoded as Base64, or {@code null}/blank
     * @return decoded image bytes, or {@code null} when the input is empty
     */
    private byte[] decodeOptionalImage(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }
        ImageValidator.validateImageBase64(imageBase64);
        return Base64.getDecoder().decode(imageBase64);
    }
}



