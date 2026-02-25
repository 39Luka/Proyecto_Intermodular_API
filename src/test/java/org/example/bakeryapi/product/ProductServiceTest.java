package org.example.bakeryapi.product;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryService;
import org.example.bakeryapi.product.exception.ProductNotFoundException;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_asUser_usesActiveOnlyRepositoryMethod() {
        setAuth(Role.USER);
        when(repository.findAllByActiveTrue(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        service.getAll(PageRequest.of(0, 10), null);

        verify(repository).findAllByActiveTrue(any(PageRequest.class));
    }

    @Test
    void getAll_asAdmin_usesFindAll() {
        setAuth(Role.ADMIN);
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        service.getAll(PageRequest.of(0, 10), null);

        verify(repository).findAll(any(PageRequest.class));
    }

    @Test
    void getById_inactive_asUser_throwsNotFound() {
        setAuth(Role.USER);
        Product inactive = new Product("Old", null, new BigDecimal("1.00"), 1, new Category("Bread"));
        inactive.disable();
        when(repository.findById(1L)).thenReturn(Optional.of(inactive));

        assertThrows(ProductNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getById_inactive_asAdmin_returnsProduct() {
        setAuth(Role.ADMIN);
        Product inactive = new Product("Old", null, new BigDecimal("1.00"), 1, new Category("Bread"));
        inactive.disable();
        when(repository.findById(1L)).thenReturn(Optional.of(inactive));

        var response = service.getById(1L);

        assertEquals(false, response.active());
        assertEquals("Old", response.name());
    }

    @Test
    void topSelling_asUser_callsActiveOnlyQuery() {
        setAuth(Role.USER);
        when(repository.findTopSellingByStatusAndActiveTrue(eq(PurchaseStatus.PAID), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getTopSelling(PageRequest.of(0, 10));

        verify(repository).findTopSellingByStatusAndActiveTrue(eq(PurchaseStatus.PAID), any(PageRequest.class));
    }

    private enum Role { USER, ADMIN }

    private void setAuth(Role role) {
        var auth = new UsernamePasswordAuthenticationToken(
                role.name().toLowerCase() + "@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}

