package org.example.bakeryapi.category;

import org.example.bakeryapi.category.dto.request.CategoryRequest;
import org.example.bakeryapi.category.exception.CategoryAlreadyExistsException;
import org.example.bakeryapi.category.exception.CategoryNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
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

    @InjectMocks
    private CategoryService service;

    @Test
    void create_duplicateName_throws() {
        when(repository.existsByNameIgnoreCase("Bread")).thenReturn(true);
        assertThrows(CategoryAlreadyExistsException.class, () -> service.create(new CategoryRequest("Bread")));
    }

    @Test
    void create_savesCategory() {
        when(repository.existsByNameIgnoreCase("Bread")).thenReturn(false);
        Category saved = new Category("Bread");
        when(repository.save(any(Category.class))).thenReturn(saved);

        var response = service.create(new CategoryRequest("Bread"));

        assertEquals("Bread", response.name());
        verify(repository).save(any(Category.class));
    }

    @Test
    void getById_missing_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getAll_returnsPage() {
        when(repository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(new Category("Bread"))));
        var page = service.getAll(PageRequest.of(0, 10));
        assertEquals(1, page.getContent().size());
        assertEquals("Bread", page.getContent().get(0).name());
    }
}
