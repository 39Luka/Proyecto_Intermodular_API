package com.bakery.bakeryapi.config;

import com.bakery.bakeryapi.config.properties.InitialAdminProperties;
import com.bakery.bakeryapi.userss.UserRepository;
import com.bakery.bakeryapi.userss.UserService;
import com.bakery.bakeryapi.userss.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InitialAdminBootstrapTest {

    private InitialAdminProperties properties;
    private UserRepository userRepository;
    private UserService userService;
    private InitialAdminBootstrap bootstrap;

    @BeforeEach
    void setUp() {
        properties = mock(InitialAdminProperties.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        bootstrap = new InitialAdminBootstrap(properties, userRepository, userService);
    }

    @Test
    void run_noVars_doesNothing() {
        when(properties.email()).thenReturn(null);
        when(properties.password()).thenReturn(null);

        bootstrap.run(mock(ApplicationArguments.class));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userService);
    }

    @Test
    void run_onlyEmail_throws() {
        when(properties.email()).thenReturn("admin@example.com");
        when(properties.password()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> bootstrap.run(mock(ApplicationArguments.class)));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userService);
    }

    @Test
    void run_onlyPassword_throws() {
        when(properties.email()).thenReturn(null);
        when(properties.password()).thenReturn("pw");

        assertThrows(IllegalStateException.class, () -> bootstrap.run(mock(ApplicationArguments.class)));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userService);
    }

    @Test
    void run_varsSetAndEmptyUsers_createsAdmin() {
        when(properties.email()).thenReturn("admin@example.com");
        when(properties.password()).thenReturn("pw");
        when(userRepository.count()).thenReturn(0L);

        bootstrap.run(mock(ApplicationArguments.class));

        verify(userService).createInternal("admin@example.com", "pw", Role.ADMIN);
    }

    @Test
    void run_varsSetButUsersExist_skips() {
        when(properties.email()).thenReturn("admin@example.com");
        when(properties.password()).thenReturn("pw");
        when(userRepository.count()).thenReturn(1L);

        bootstrap.run(mock(ApplicationArguments.class));

        verify(userService, never()).createInternal(anyString(), anyString(), any(Role.class));
    }
}
