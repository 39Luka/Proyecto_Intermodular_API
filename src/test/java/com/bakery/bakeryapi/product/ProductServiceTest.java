package com.bakery.bakeryapi.product;

import com.bakery.bakeryapi.category.CategoryService;
import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.product.exception.ProductNotFoundException;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import com.bakery.bakeryapi.repository.ProductRepository;
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

    @Mock
    private PaginationProperties paginationProperties;

    @InjectMocks
    private ProductService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * CP-PRD.01: getAll_asUser_usesActiveOnlyRepositoryMethod
     * Valida que cuando un usuario normal solicita productos, solo se devuelvan los que están marcados como activos.
     */
    @Test
    void getAll_asUser_usesActiveOnlyRepositoryMethod() {
        when(paginationProperties.maxPageSize()).thenReturn(100);
        setAuth(Role.USER);
        when(repository.findAllByActiveTrue(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        service.getAll(PageRequest.of(0, 10), null, null);

        verify(repository).findAllByActiveTrue(any(PageRequest.class));
    }

    /**
     * CP-PRD.02: getAll_asAdmin_usesFindAll
     * Verifica que un administrador pueda ver todos los productos del catálogo, incluidos los inactivos.
     */
    @Test
    void getAll_asAdmin_usesFindAll() {
        when(paginationProperties.maxPageSize()).thenReturn(100);
        setAuth(Role.ADMIN);
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        service.getAll(PageRequest.of(0, 10), null, null);

        verify(repository).findAll(any(PageRequest.class));
    }

    /**
     * CP-PRD.03: getById_inactive_asUser_throwsNotFound
     * Asegura que un usuario no pueda acceder a la ficha de un producto que ha sido desactivado.
     */
    @Test
    void getById_inactive_asUser_throwsNotFound() {
        setAuth(Role.USER);
        Product inactive = new Product("Old", null, new BigDecimal("1.00"), 1, new Category("Bread"));
        inactive.disable();
        when(repository.findById(1L)).thenReturn(Optional.of(inactive));

        assertThrows(ProductNotFoundException.class, () -> service.getById(1L));
    }

    /**
     * CP-PRD.04: getById_inactive_asAdmin_returnsProduct
     * Valida que un administrador sí pueda consultar la información de productos inactivos por su ID.
     */
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

    /**
     * CP-PRD.05: topSelling_asUser_callsActiveOnlyQuery
     * Verifica que el listado de productos más vendidos para usuarios solo incluya productos activos.
     */
    @Test
    void topSelling_asUser_callsActiveOnlyQuery() {
        when(paginationProperties.maxPageSize()).thenReturn(100);
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

