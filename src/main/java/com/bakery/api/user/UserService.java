package com.bakery.api.user;

import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
import com.bakery.api.user.dto.request.UserRequest;
import com.bakery.api.user.dto.response.UserResponse;
import com.bakery.api.user.exception.EmailAlreadyExistsException;
import com.bakery.api.user.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User getEntityByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(getEntityById(id));
    }

    public UserResponse getByEmail(String email) {
        return UserResponse.from(getEntityByEmail(email));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = createInternal(request.email(), request.password(), request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public User createInternal(String email, String password, Role role) {
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, role);
        return repository.save(user);
    }

    @Transactional
    public void enableUser(Long id) {
        User user = getEntityById(id);
        user.enable();
        repository.save(user);
    }

    @Transactional
    public void disableUser(Long id) {
        User user = getEntityById(id);
        user.disable();
        repository.save(user);
    }

}



