package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    // DI prin constructor
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books
    @GetMapping
    public List<Book> getAll() {
        return bookService.getAllBooks();
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public Book getById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // POST /api/books
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@Valid @RequestBody Book book) {
        return bookService.createBook(book);
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @Valid @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/search/title")
    public List<Book> searchByTitle(@RequestParam String title) {
        return bookService.searchByTitle(title);
    }

    @GetMapping("/search/author")
    public List<Book> searchByAuthor(@RequestParam String author) {
        return bookService.searchByAuthor(author);
    }

    @GetMapping("/search/category")
    public List<Book> searchByCategory(@RequestParam String category) {
        return bookService.searchByCategoryName(category);
    }
    // GET /api/books/search?keyword=clean
    @GetMapping("/search")
    public List<Book> search(@RequestParam String keyword) {
        return bookService.searchAnywhere(keyword);
    }

}
