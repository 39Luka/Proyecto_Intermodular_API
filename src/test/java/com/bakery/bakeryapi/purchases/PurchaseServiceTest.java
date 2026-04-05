package com.bakery.bakeryapi.purchasess;

import com.bakery.bakeryapi.auth.exception.ForbiddenOperationException;
import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.product.Product;
import com.bakery.bakeryapi.catalog.product.ProductService;
import com.bakery.bakeryapi.promotionss.PromotionService;
import com.bakery.bakeryapi.promotionss.Promotion;
import com.bakery.bakeryapi.purchasess.Purchase;
import com.bakery.bakeryapi.purchasess.PurchaseItem;
import com.bakery.bakeryapi.purchasess.PurchaseStatus;
import com.bakery.bakeryapi.purchasess.dto.PurchaseMapper;
import com.bakery.bakeryapi.purchasess.dto.PurchaseItemRequest;
import com.bakery.bakeryapi.purchasess.dto.PurchaseRequest;
import com.bakery.bakeryapi.purchasess.dto.PurchaseItemResponse;
import com.bakery.bakeryapi.purchasess.dto.PurchaseResponse;
import com.bakery.bakeryapi.purchasess.exception.InvalidPurchaseException;
import com.bakery.bakeryapi.userss.UserService;
import com.bakery.bakeryapi.userss.domain.Role;
import com.bakery.bakeryapi.userss.domain.User;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

    @Mock
    private PurchaseMapper mapper;

    @Mock
    private Clock clock;

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
        when(clock.instant()).thenReturn(Instant.parse("2026-03-21T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        User target = userWithId(2L, "target@example.com", Role.USER);
        when(userService.getEntityById(2L)).thenReturn(target);

        Category category = new Category("Bread");
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, category);
        when(productService.getActiveEntityById(10L)).thenReturn(product);
        when(repository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            List<PurchaseItemResponse> items = p.getItems().stream()
                    .map(i -> new PurchaseItemResponse(
                            i.getProduct().getId(),
                            i.getProduct().getName(),
                            i.getQuantity(),
                            i.getUnitPrice(),
                            i.getDiscountAmount(),
                            i.getSubtotal(),
                            i.getPromotion() == null ? null : i.getPromotion().getId()
                    ))
                    .toList();
            return new PurchaseResponse(
                    p.getId(),
                    p.getUser().getId(),
                    p.getCreatedAt(),
                    p.getStatus(),
                    p.getTotal(),
                    items
            );
        });

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

