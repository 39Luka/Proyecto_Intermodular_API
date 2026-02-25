package org.example.bakeryapi.promotion;

import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.product.ProductService;
import org.example.bakeryapi.promotion.domain.Promotion;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository repository;

    @Mock
    private ProductService productService;

    @Mock
    private PromotionUsageRepository usageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PromotionService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_capsPageSizeTo100() {
        setAuth(Role.ADMIN, "admin@example.com");
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.getAll(PageRequest.of(0, 1000));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void getActiveByProduct_asUser_usesAuthenticatedUserId_whenUserIdNull() {
        setAuth(Role.USER, "user@example.com");
        User current = userWithId(10L, "user@example.com", Role.USER);
        when(userService.getEntityByEmail("user@example.com")).thenReturn(current);
        when(repository.findActiveByProductIdAndUserId(eq(5L), eq(10L), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getActiveByProduct(5L, null, PageRequest.of(0, 10));

        verify(repository).findActiveByProductIdAndUserId(eq(5L), eq(10L), any(LocalDate.class), any(Pageable.class));
        verify(repository, never()).findActiveByProductId(eq(5L), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void getActiveByProduct_asUser_withDifferentUserId_throwsForbidden() {
        setAuth(Role.USER, "user@example.com");
        User current = userWithId(10L, "user@example.com", Role.USER);
        when(userService.getEntityByEmail("user@example.com")).thenReturn(current);

        assertThrows(ForbiddenOperationException.class,
                () -> service.getActiveByProduct(5L, 99L, PageRequest.of(0, 10)));
    }

    @Test
    void getActiveByProduct_asAdmin_withUserId_validatesUser_andFilters() {
        setAuth(Role.ADMIN, "admin@example.com");
        when(repository.findActiveByProductIdAndUserId(eq(5L), eq(10L), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getActiveByProduct(5L, 10L, PageRequest.of(0, 10));

        verify(userService).getEntityById(10L);
        verify(repository).findActiveByProductIdAndUserId(eq(5L), eq(10L), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void getActiveByProduct_asAdmin_withNullUserId_returnsAllActive() {
        setAuth(Role.ADMIN, "admin@example.com");
        when(repository.findActiveByProductId(eq(5L), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getActiveByProduct(5L, null, PageRequest.of(0, 10));

        verify(repository).findActiveByProductId(eq(5L), any(LocalDate.class), any(Pageable.class));
        verify(repository, never()).findActiveByProductIdAndUserId(eq(5L), any(Long.class), any(LocalDate.class), any(Pageable.class));
    }

    private void setAuth(Role role, String email) {
        var auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private User userWithId(Long id, String email, Role role) {
        User user = new User(email, "pass", role);
        setEntityId(user, id);
        return user;
    }

    private void setEntityId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

