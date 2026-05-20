package com.bakery.bakeryapi.category;

import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.category.dto.CategoryRequest;
import com.bakery.bakeryapi.category.exception.CategoryAlreadyExistsException;
import com.bakery.bakeryapi.category.exception.CategoryNotFoundException;
import com.bakery.bakeryapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private PaginationProperties paginationProperties;

    @InjectMocks
    private CategoryService service;

    /**
     * CP-CAT.07: create_duplicateName_throws
     * Asegura que el servicio lance una excepción si se intenta crear una categoría con un nombre que ya existe (ignorando mayúsculas).
     */
    @Test
    void create_duplicateName_throws() {
        when(repository.existsByNameIgnoreCase("Bread")).thenReturn(true);
        assertThrows(CategoryAlreadyExistsException.class, () -> service.create(new CategoryRequest("Bread")));
    }

    /**
     * CP-CAT.08: create_savesCategory
     * Valida la lógica de negocio para guardar una nueva categoría válida en el repositorio.
     */
    @Test
    void create_savesCategory() {
        when(repository.existsByNameIgnoreCase("Bread")).thenReturn(false);
        Category saved = new Category("Bread");
        when(repository.save(any(Category.class))).thenReturn(saved);

        var response = service.create(new CategoryRequest("Bread"));

        assertEquals("Bread", response.name());
        verify(repository).save(any(Category.class));
    }

    /**
     * CP-CAT.09: getById_missing_throws
     * Verifica que el servicio lance una excepción si se solicita una categoría por un ID inexistente.
     */
    @Test
    void getById_missing_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> service.getById(1L));
    }

    /**
     * CP-CAT.10: getAll_returnsPage
     * Valida que el listado de categorías sea paginado correctamente y devuelva los datos esperados.
     */
    @Test
    void getAll_returnsPage() {
        when(paginationProperties.maxPageSize()).thenReturn(100);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new Category("Bread"))));
        var page = service.getAll(PageRequest.of(0, 10));
        assertEquals(1, page.getContent().size());
        assertEquals("Bread", page.getContent().get(0).name());
    }
}
