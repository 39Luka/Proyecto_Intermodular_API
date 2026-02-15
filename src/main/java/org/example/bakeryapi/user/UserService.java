package org.example.bakeryapi.user;

import org.example.bakeryapi.user.exception.EmailAlreadyExistsException;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User create(String email, String password, Role role) {
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(email, password, role);
        return repository.save(user);
    }

    public void enableUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.enable();
        repository.save(user);
    }


    public void disableUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.disable();
        repository.save(user);
    }

}

