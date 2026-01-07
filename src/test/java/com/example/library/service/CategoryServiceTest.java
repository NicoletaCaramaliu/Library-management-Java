package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Category;
import com.example.library.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");
        category1.setDescription("Fiction books");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");
        category2.setDescription("Science books");
    }

    @Test
    void getAllCategories_shouldReturnList() {
        // given
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        // when
        List<Category> result = categoryService.getAllCategories();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(category1, category2);
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() {
        // given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        // when
        Category result = categoryService.getCategoryById(1L);

        // then
        assertThat(result).isEqualTo(category1);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_shouldThrowBusinessException_whenNotFound() {
        // given
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> categoryService.getCategoryById(99L)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Category not found with id: 99");
        verify(categoryRepository).findById(99L);
    }

    @Test
    void createCategory_shouldSetIdNullAndSave() {
        // given
        Category toCreate = new Category();
        toCreate.setId(123L); // va fi ignorat
        toCreate.setName("New");
        toCreate.setDescription("New cat");

        Category saved = new Category();
        saved.setId(10L);
        saved.setName("New");
        saved.setDescription("New cat");

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        // when
        Category result = categoryService.createCategory(toCreate);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        verify(categoryRepository).save(argThat(cat ->
                cat.getId() == null &&
                        "New".equals(cat.getName()) &&
                        "New cat".equals(cat.getDescription())
        ));
    }

    @Test
    void updateCategory_shouldUpdateFieldsAndSave_whenExists() {
        // given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        Category updated = new Category();
        updated.setName("Updated name");
        updated.setDescription("Updated description");

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Category result = categoryService.updateCategory(1L, updated);

        // then
        assertThat(result.getName()).isEqualTo("Updated name");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getId()).isEqualTo(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category1);
    }

    @Test
    void updateCategory_shouldThrowBusinessException_whenNotFound() {
        // given
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        Category updated = new Category();
        updated.setName("Whatever");
        updated.setDescription("Whatever");

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> categoryService.updateCategory(5L, updated)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(categoryRepository).findById(5L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_shouldDelete_whenExists() {
        // given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        // when
        categoryService.deleteCategory(1L);

        // then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category1);
    }

    @Test
    void deleteCategory_shouldThrowBusinessException_whenNotFound() {
        // given
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> categoryService.deleteCategory(3L)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(categoryRepository).findById(3L);
        verify(categoryRepository, never()).delete(any());
    }
}
