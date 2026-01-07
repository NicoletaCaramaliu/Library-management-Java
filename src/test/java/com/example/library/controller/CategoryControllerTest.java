package com.example.library.controller;

import com.example.library.model.Category;
import com.example.library.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category buildCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/categories ")
    void getAll_shouldReturnListOfCategories() throws Exception {
        List<Category> categories = Arrays.asList(
                buildCategory(1L, "Fiction", "Fiction books"),
                buildCategory(2L, "Science", "Science books")
        );
        Mockito.when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Fiction")))
                .andExpect(jsonPath("$[0].description", is("Fiction books")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Science")))
                .andExpect(jsonPath("$[1].description", is("Science books")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/categories/{id} ")
    void getById_shouldReturnCategory() throws Exception {
        Category category = buildCategory(1L, "Fiction", "Fiction books");
        Mockito.when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Fiction")))
                .andExpect(jsonPath("$.description", is("Fiction books")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/categories")
    void create_shouldCreateCategory() throws Exception {
        Category requestCategory = buildCategory(null, "Fiction", "Fiction books");
        Category savedCategory = buildCategory(1L, "Fiction", "Fiction books");

        Mockito.when(categoryService.createCategory(any(Category.class))).thenReturn(savedCategory);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())   // CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Fiction")))
                .andExpect(jsonPath("$.description", is("Fiction books")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/categories/{id}")
    void update_shouldUpdateCategory() throws Exception {
        Category requestCategory = buildCategory(null, "Updated", "Updated description");
        Category updatedCategory = buildCategory(1L, "Updated", "Updated description");

        Mockito.when(categoryService.updateCategory(eq(1L), any(Category.class)))
                .thenReturn(updatedCategory);

        mockMvc.perform(put("/api/categories/{id}", 1L)
                        .with(csrf())   // CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/categories/{id}")
    void delete_shouldDeleteCategory() throws Exception {
        Mockito.doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L)
                        .with(csrf()))   // CSRF token
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).deleteCategory(1L);
    }
}
