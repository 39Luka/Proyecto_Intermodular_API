package com.bakery.bakeryapi.user;

import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import com.bakery.bakeryapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_existingUser_returnsUser() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.getById(1L);

        assertEquals("silvia@example.com", result.email());
    }

    @Test
    void getById_nonExistingUser_throwsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void create_uniqueEmail_savesUser() {
        when(repository.existsByEmail("silvia@example.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        User user = new User("silvia@example.com", "hashed", Role.USER);
        when(repository.save(any(User.class))).thenReturn(user);

        var result = userService.create(new UserRequest("silvia@example.com", "1234", Role.USER));

        assertEquals("silvia@example.com", result.email());
        verify(passwordEncoder).encode("1234");
    }

    @Test
    void create_existingEmail_throwsException() {
        when(repository.existsByEmail("silvia@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create(new UserRequest("silvia@example.com", "1234", Role.USER)));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void enableUser_existingUser_setsEnabledTrue() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        user.disable();
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.setEnabled(1L, true);

        assertTrue(user.isEnabled());
        verify(repository).save(user);
    }

    @Test
    void disableUser_existingUser_setsEnabledFalse() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.setEnabled(1L, false);

        assertFalse(user.isEnabled());
        verify(repository).save(user);
    }

    @Test
    void updateProfileImage_validImage_savesImage() {
        String pngBase64 = Base64.getEncoder().encodeToString(new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        });
        User user = new User("silvia@example.com", "1234", Role.USER);
        when(repository.findByEmail("silvia@example.com")).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        var result = userService.updateProfileImage("silvia@example.com", pngBase64);

        assertArrayEquals(Base64.getDecoder().decode(pngBase64), user.getProfileImage());
        assertEquals(pngBase64, result.profileImageBase64());
        verify(repository).save(user);
    }

    @Test
    void updateProfileImage_blankImage_removesImage() {
        User user = new User("silvia@example.com", "1234", Role.USER);
        user.setProfileImage(new byte[]{1, 2, 3});
        when(repository.findByEmail("silvia@example.com")).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        var result = userService.updateProfileImage("silvia@example.com", "");

        assertNull(user.getProfileImage());
        assertNull(result.profileImageBase64());
        verify(repository).save(user);
    }

    @Test
    void changePassword_correctCurrentPassword_savesEncodedPasswordAndRotatesRefreshToken() {
        User user = new User("silvia@example.com", "old-hashed", Role.USER);
        when(repository.findByEmail("silvia@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "old-hashed")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hashed");

        userService.changePassword("silvia@example.com", "oldPassword", "newPassword123");

        assertEquals("new-hashed", user.getPassword());
        assertEquals(1, user.getRefreshTokenVersion());
        verify(repository).save(user);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsInvalidCredentialsException() {
        User user = new User("silvia@example.com", "old-hashed", Role.USER);
        when(repository.findByEmail("silvia@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "old-hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.changePassword("silvia@example.com", "wrongPassword", "newPassword123"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

}


