package org.example.bakeryapi.user;

import org.example.bakeryapi.user.exception.EmailAlreadyExistsException;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_existingUser_returnsUser() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertEquals("silvia@example.com", result.getEmail());
    }

    @Test
    void getById_nonExistingUser_throwsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void create_uniqueEmail_savesUser() {
        when(repository.existsByEmail("silvia@example.com")).thenReturn(false);
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.save(any(User.class))).thenReturn(user);

        User result = userService.create("silvia@example.com", "1234", Role.USER);

        assertEquals("silvia@example.com", result.getEmail());
    }

    @Test
    void create_existingEmail_throwsException() {
        when(repository.existsByEmail("silvia@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create("silvia@example.com", "1234", Role.USER));
    }

    @Test
    void enableUser_existingUser_setsEnabledTrue() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        user.disable();
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.enableUser(1L);

        assertTrue(user.isEnabled());
        verify(repository).save(user);
    }

    @Test
    void disableUser_existingUser_setsEnabledFalse() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.disableUser(1L);

        assertFalse(user.isEnabled());
        verify(repository).save(user);
    }

}
