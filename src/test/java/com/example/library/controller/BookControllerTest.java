package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Category;
import com.example.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book buildBook(Long id, String title, String author, String categoryName) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn("123-456-789");
        book.setPublishedYear(2020);
        book.setAvailableCopies(5);

        Category category = new Category();
        category.setId(10L);
        category.setName(categoryName);
        book.setCategory(category);

        return book;
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books")
    void getAll_shouldReturnListOfBooks() throws Exception {
        List<Book> books = Arrays.asList(
                buildBook(1L, "Clean Code", "Robert Martin", "Programming"),
                buildBook(2L, "Effective Java", "Joshua Bloch", "Programming")
        );
        Mockito.when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[0].author", is("Robert Martin")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Effective Java")))
                .andExpect(jsonPath("$[1].author", is("Joshua Bloch")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/{id} ")
    void getById_shouldReturnBook() throws Exception {
        Book book = buildBook(1L, "Clean Code", "Robert Martin", "Programming");
        Mockito.when(bookService.getBookById(1L)).thenReturn(book);

        mockMvc.perform(get("/api/books/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert Martin")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/books ")
    void create_shouldCreateBook() throws Exception {
        Book requestBook = buildBook(null, "Clean Code", "Robert Martin", "Programming");
        Book savedBook = buildBook(1L, "Clean Code", "Robert Martin", "Programming");

        Mockito.when(bookService.createBook(any(Book.class))).thenReturn(savedBook);

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/books/{id} ")
    void update_shouldUpdateBook() throws Exception {
        Book requestBook = buildBook(null, "Clean Code (2nd ed.)", "Robert Martin", "Programming");
        Book updatedBook = buildBook(1L, "Clean Code (2nd ed.)", "Robert Martin", "Programming");

        Mockito.when(bookService.updateBook(eq(1L), any(Book.class)))
                .thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code (2nd ed.)")));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/books/{id} ")
    void delete_shouldDeleteBook() throws Exception {
        Mockito.doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(bookService).deleteBook(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/search/title ")
    void searchByTitle_shouldReturnBooks() throws Exception {
        List<Book> books = List.of(
                buildBook(1L, "Clean Code", "Robert Martin", "Programming")
        );
        Mockito.when(bookService.searchByTitle("Clean"))
                .thenReturn(books);

        mockMvc.perform(get("/api/books/search/title")
                        .param("title", "Clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/search/author ")
    void searchByAuthor_shouldReturnBooks() throws Exception {
        List<Book> books = List.of(
                buildBook(1L, "Clean Code", "Robert Martin", "Programming")
        );
        Mockito.when(bookService.searchByAuthor("Martin"))
                .thenReturn(books);

        mockMvc.perform(get("/api/books/search/author")
                        .param("author", "Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("Robert Martin")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/search/category ")
    void searchByCategory_shouldReturnBooks() throws Exception {
        List<Book> books = List.of(
                buildBook(1L, "Clean Code", "Robert Martin", "Programming")
        );
        Mockito.when(bookService.searchByCategoryName("Programming"))
                .thenReturn(books);

        mockMvc.perform(get("/api/books/search/category")
                        .param("category", "Programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category.name", is("Programming")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/search?keyword=... - cauta peste tot")
    void search_shouldReturnBooks() throws Exception {
        List<Book> books = List.of(
                buildBook(1L, "Clean Code", "Robert Martin", "Programming")
        );
        Mockito.when(bookService.searchAnywhere("clean"))
                .thenReturn(books);

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }
}
