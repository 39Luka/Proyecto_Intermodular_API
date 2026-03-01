package org.example.bakeryapi.purchase;

import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductService;
import org.example.bakeryapi.promotion.PromotionService;
import org.example.bakeryapi.promotion.domain.Promotion;
import org.example.bakeryapi.purchase.domain.Purchase;
import org.example.bakeryapi.purchase.domain.PurchaseItem;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.example.bakeryapi.purchase.dto.request.PurchaseItemRequest;
import org.example.bakeryapi.purchase.dto.request.PurchaseRequest;
import org.example.bakeryapi.purchase.exception.InvalidPurchaseException;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private PurchaseService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_withoutItems_throws() {
        setAuth(Role.USER, "user@example.com");
        PurchaseRequest request = new PurchaseRequest(1L, List.of());
        assertThrows(InvalidPurchaseException.class, () -> service.create(request));
    }

    @Test
    void create_asUser_forAnotherUser_throwsForbidden() {
        setAuth(Role.USER, "user@example.com");
        User current = userWithId(1L, "user@example.com", Role.USER);
        when(userService.getEntityByEmail("user@example.com")).thenReturn(current);

        PurchaseRequest request = new PurchaseRequest(2L, List.of(new PurchaseItemRequest(10L, 1, null)));
        assertThrows(ForbiddenOperationException.class, () -> service.create(request));
        verify(userService, never()).getEntityById(any());
    }

    @Test
    void create_asAdmin_forAnotherUser_loadsUserById() {
        setAuth(Role.ADMIN, "admin@example.com");
        User target = userWithId(2L, "target@example.com", Role.USER);
        when(userService.getEntityById(2L)).thenReturn(target);

        Category category = new Category("Bread");
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, category);
        when(productService.getActiveEntityById(10L)).thenReturn(product);
        when(repository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequest request = new PurchaseRequest(2L, List.of(new PurchaseItemRequest(10L, 2, null)));
        var response = service.create(request);

        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("2.00"), response.total());
        verify(userService).getEntityById(2L);
    }

    @Test
    void cancel_whenPaid_throws() {
        setAuth(Role.USER, "user@example.com");
        User owner = userWithId(1L, "user@example.com", Role.USER);
        when(userService.getEntityByEmail("user@example.com")).thenReturn(owner);

        Purchase purchase = new Purchase(owner, LocalDateTime.now(), PurchaseStatus.CREATED);
        purchase.pay();
        setEntityId(purchase, 10L);
        when(repository.findDetailedById(10L)).thenReturn(Optional.of(purchase));

        assertThrows(InvalidPurchaseException.class, () -> service.cancel(10L));
    }

    @Test
    void cancel_created_restoresStock_andReleasesPromotionUsage() {
        setAuth(Role.USER, "user@example.com");
        User owner = userWithId(1L, "user@example.com", Role.USER);
        when(userService.getEntityByEmail("user@example.com")).thenReturn(owner);

        Category category = new Category("Bread");
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 3, category);
        Promotion promotion = org.mockito.Mockito.mock(Promotion.class);
        PurchaseItem item = new PurchaseItem(product, promotion, 2, product.getPrice(), BigDecimal.ZERO, new BigDecimal("2.00"));

        Purchase purchase = new Purchase(owner, LocalDateTime.now(), PurchaseStatus.CREATED);
        purchase.addItem(item);
        setEntityId(purchase, 10L);

        when(repository.findDetailedById(10L)).thenReturn(Optional.of(purchase));
        when(repository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cancel(10L);

        assertEquals(5, product.getStock());
        verify(promotionService).releaseUsage(eq(promotion), eq(owner));
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

