package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book One");
        book1.setAuthor("Author A");
        book1.setIsbn("ISBN-1");
        book1.setPublishedYear(2000);
        book1.setAvailableCopies(3);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book Two");
        book2.setAuthor("Author B");
        book2.setIsbn("ISBN-2");
        book2.setPublishedYear(2010);
        book2.setAvailableCopies(5);
    }

    @Test
    void getAllBooks_shouldReturnList() {
        // given
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        // when
        List<Book> result = bookService.getAllBooks();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(book1, book2);
        verify(bookRepository).findAll();
    }

    @Test
    void getBookById_shouldReturnBook_whenExists() {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        // when
        Book result = bookService.getBookById(1L);

        // then
        assertThat(result).isEqualTo(book1);
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_shouldThrowBusinessException_whenNotFound() {
        // given
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> bookService.getBookById(99L)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Book not found with id: 99");
        verify(bookRepository).findById(99L);
    }

    @Test
    void createBook_shouldSetIdNullAndSave() {
        // given
        Book toCreate = new Book();
        toCreate.setId(123L); // va fi ignorat
        toCreate.setTitle("New Book");
        toCreate.setAuthor("New Author");
        toCreate.setIsbn("NEW-ISBN");
        toCreate.setPublishedYear(2024);
        toCreate.setAvailableCopies(10);

        Book saved = new Book();
        saved.setId(10L);
        saved.setTitle("New Book");
        saved.setAuthor("New Author");
        saved.setIsbn("NEW-ISBN");
        saved.setPublishedYear(2024);
        saved.setAvailableCopies(10);

        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // when
        Book result = bookService.createBook(toCreate);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        verify(bookRepository).save(argThat(book ->
                book.getId() == null &&
                        "New Book".equals(book.getTitle()) &&
                        "New Author".equals(book.getAuthor()) &&
                        "NEW-ISBN".equals(book.getIsbn()) &&
                        book.getPublishedYear() == 2024 &&
                        book.getAvailableCopies() == 10
        ));
    }

    @Test
    void updateBook_shouldUpdateFieldsAndSave_whenExists() {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Book updated = new Book();
        updated.setTitle("Updated Title");
        updated.setAuthor("Updated Author");
        updated.setIsbn("UPDATED-ISBN");
        updated.setPublishedYear(2022);
        updated.setAvailableCopies(7);

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Book result = bookService.updateBook(1L, updated);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getAuthor()).isEqualTo("Updated Author");
        assertThat(result.getIsbn()).isEqualTo("UPDATED-ISBN");
        assertThat(result.getPublishedYear()).isEqualTo(2022);
        assertThat(result.getAvailableCopies()).isEqualTo(7);

        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book1);
    }

    @Test
    void updateBook_shouldThrowBusinessException_whenNotFound() {
        // given
        when(bookRepository.findById(5L)).thenReturn(Optional.empty());

        Book updated = new Book();
        updated.setTitle("Whatever");

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> bookService.updateBook(5L, updated)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookRepository).findById(5L);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBook_shouldDelete_whenExists() {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        // when
        bookService.deleteBook(1L);

        // then
        verify(bookRepository).findById(1L);
        verify(bookRepository).delete(book1);
    }

    @Test
    void deleteBook_shouldThrowBusinessException_whenNotFound() {
        // given
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> bookService.deleteBook(3L)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookRepository).findById(3L);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    void searchByTitle_shouldUseRepositoryMethod() {
        // given
        when(bookRepository.findByTitleContainingIgnoreCase("book"))
                .thenReturn(List.of(book1));

        // when
        List<Book> result = bookService.searchByTitle("book");

        // then
        assertThat(result).containsExactly(book1);
        verify(bookRepository).findByTitleContainingIgnoreCase("book");
    }

    @Test
    void searchByAuthor_shouldUseRepositoryMethod() {
        // given
        when(bookRepository.findByAuthorContainingIgnoreCase("author"))
                .thenReturn(List.of(book2));

        // when
        List<Book> result = bookService.searchByAuthor("author");

        // then
        assertThat(result).containsExactly(book2);
        verify(bookRepository).findByAuthorContainingIgnoreCase("author");
    }

    @Test
    void searchByCategoryName_shouldUseRepositoryMethod() {
        // given
        when(bookRepository.findByCategory_NameIgnoreCase("fiction"))
                .thenReturn(List.of(book1, book2));

        // when
        List<Book> result = bookService.searchByCategoryName("fiction");

        // then
        assertThat(result).containsExactly(book1, book2);
        verify(bookRepository).findByCategory_NameIgnoreCase("fiction");
    }

    @Test
    void searchAnywhere_shouldUseRepositoryMethod() {
        // given
        when(bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
                        "key", "key", "key"))
                .thenReturn(List.of(book1));

        // when
        List<Book> result = bookService.searchAnywhere("key");

        // then
        assertThat(result).containsExactly(book1);
        verify(bookRepository)
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
                        "key", "key", "key"
                );
    }
}
