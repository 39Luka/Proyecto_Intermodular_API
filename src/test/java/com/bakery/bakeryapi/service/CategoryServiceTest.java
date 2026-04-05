package com.bakery.bakeryapi.service;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.dto.category.CategoryMapper;
import com.bakery.bakeryapi.dto.category.CategoryRequest;
import com.bakery.bakeryapi.dto.category.CategoryResponse;
import com.bakery.bakeryapi.exception.category.CategoryAlreadyExistsException;
import com.bakery.bakeryapi.exception.category.CategoryNotFoundException;
import com.bakery.bakeryapi.repository.CategoryRepository;
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

    @Mock
    private CategoryMapper mapper;

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
        when(mapper.toResponse(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            return new CategoryResponse(c.getId(), c.getName());
        });

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
        when(mapper.toResponse(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            return new CategoryResponse(c.getId(), c.getName());
        });
        var page = service.getAll(PageRequest.of(0, 10));
        assertEquals(1, page.getContent().size());
        assertEquals("Bread", page.getContent().get(0).name());
    }
}
