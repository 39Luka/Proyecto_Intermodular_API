package com.bakery.bakeryapi.userss;

import com.bakery.bakeryapi.userss.domain.Role;
import com.bakery.bakeryapi.userss.domain.User;
import com.bakery.bakeryapi.userss.dto.UserMapper;
import com.bakery.bakeryapi.userss.dto.UserRequest;
import com.bakery.bakeryapi.userss.dto.UserResponse;
import com.bakery.bakeryapi.userss.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.userss.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, UserMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
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
        return mapper.toResponse(getEntityById(id));
    }

    public UserResponse getByEmail(String email) {
        return mapper.toResponse(getEntityByEmail(email));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = createInternal(request.email(), request.password(), request.role());
        return mapper.toResponse(user);
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
    public void setEnabled(Long id, boolean enabled) {
        User user = getEntityById(id);
        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }
        repository.save(user);
    }

    @Transactional
    @Deprecated(forRemoval = true)
    public void enableUser(Long id) {
        setEnabled(id, true);
    }

    @Transactional
    @Deprecated(forRemoval = true)
    public void disableUser(Long id) {
        setEnabled(id, false);
    }

}



