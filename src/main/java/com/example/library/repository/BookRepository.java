package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByCategory_NameIgnoreCase(String categoryName);
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
            String title,
            String author,
            String category
    );

}
