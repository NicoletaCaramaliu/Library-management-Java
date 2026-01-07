package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private User librarian;
    private Book book;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("User One");
        user.setRole(Role.USER);

        librarian = new User();
        librarian.setId(2L);
        librarian.setEmail("librarian@test.com");
        librarian.setName("Lib Rarian");
        librarian.setRole(Role.LIBRARIAN);

        book = new Book();
        book.setId(10L);
        book.setTitle("Test Book");
        book.setAuthor("Author");

        review = new Review();
        review.setId(100L);
        review.setUser(user);
        review.setBook(book);
        review.setRating(4);
        review.setComment("Nice");
        review.setCreatedAt(LocalDateTime.now());
    }


    @Test
    void createReview_shouldSaveReview_whenDataIsValid() {
        // given
        Review input = new Review();
        Book tmpBook = new Book();
        tmpBook.setId(book.getId());
        input.setBook(tmpBook);
        input.setRating(5);
        input.setComment("Great!");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review saved = invocation.getArgument(0);
                    saved.setId(123L);
                    return saved;
                });

        // when
        Review result = reviewService.createReview(user.getEmail(), input);

        // then
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great!");
        assertThat(result.getCreatedAt()).isNotNull();

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getBook()).isEqualTo(book);
    }

    @Test
    void createReview_shouldThrow_whenUserNotFound() {
        // given
        Review input = new Review();
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(user.getEmail(), input)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with email");
        verifyNoInteractions(bookRepository, reviewRepository);
    }

    @Test
    void createReview_shouldThrow_whenBookIdMissing() {
        // given
        Review input = new Review(); // fara book
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(user.getEmail(), input)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Book id is required");
        verifyNoInteractions(bookRepository, reviewRepository);
    }

    @Test
    void createReview_shouldThrow_whenBookNotFound() {
        // given
        Review input = new Review();
        Book tmpBook = new Book();
        tmpBook.setId(book.getId());
        input.setBook(tmpBook);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId()))
                .thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.createReview(user.getEmail(), input)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Book not found with id");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getAllReviews_shouldReturnList() {
        // given
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        // when
        List<Review> result = reviewService.getAllReviews();

        // then
        assertThat(result).containsExactly(review);
        verify(reviewRepository).findAll();
    }


    @Test
    void getReviewsForBook_shouldUseRepository() {
        // given
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(book.getId()))
                .thenReturn(List.of(review));

        // when
        List<Review> result = reviewService.getReviewsForBook(book.getId());

        // then
        assertThat(result).containsExactly(review);
        verify(reviewRepository).findByBookIdOrderByCreatedAtDesc(book.getId());
    }


    @Test
    void getReviewsForUserEmail_shouldReturnList_whenUserExists() {
        // given
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(review));

        // when
        List<Review> result = reviewService.getReviewsForUserEmail(user.getEmail());

        // then
        assertThat(result).containsExactly(review);
        verify(userRepository).findByEmail(user.getEmail());
        verify(reviewRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Test
    void getReviewsForUserEmail_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.getReviewsForUserEmail(user.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).findByEmail(user.getEmail());
        verify(reviewRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getAverageRatingForBook_shouldReturnZero_whenNull() {
        // given
        when(reviewRepository.findAverageRatingByBookId(book.getId()))
                .thenReturn(null);

        // when
        Double avg = reviewService.getAverageRatingForBook(book.getId());

        // then
        assertThat(avg).isEqualTo(0.0);
        verify(reviewRepository).findAverageRatingByBookId(book.getId());
    }

    @Test
    void getAverageRatingForBook_shouldReturnValue_whenExists() {
        // given
        when(reviewRepository.findAverageRatingByBookId(book.getId()))
                .thenReturn(4.5);

        // when
        Double avg = reviewService.getAverageRatingForBook(book.getId());

        // then
        assertThat(avg).isEqualTo(4.5);
        verify(reviewRepository).findAverageRatingByBookId(book.getId());
    }


    @Test
    void deleteReview_shouldThrow_whenReviewNotFound() {
        // given
        when(reviewRepository.findById(100L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.deleteReview(100L, user.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Review not found with id: 100");
        verify(reviewRepository).findById(100L);
        verify(userRepository, never()).findByEmail(anyString());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_shouldThrow_whenUserNotFound() {
        // given
        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.deleteReview(review.getId(), user.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with email");
        verify(reviewRepository).findById(review.getId());
        verify(userRepository).findByEmail(user.getEmail());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_shouldAllowLibrarianToDeleteAnyReview() {
        // given
        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));
        when(userRepository.findByEmail(librarian.getEmail()))
                .thenReturn(Optional.of(librarian));

        // when
        reviewService.deleteReview(review.getId(), librarian.getEmail());

        // then
        verify(reviewRepository).findById(review.getId());
        verify(userRepository).findByEmail(librarian.getEmail());
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_shouldAllowAdminToDeleteAnyReview() {
        // given
        User admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));
        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));

        // when
        reviewService.deleteReview(review.getId(), admin.getEmail());

        // then
        verify(reviewRepository).findById(review.getId());
        verify(userRepository).findByEmail(admin.getEmail());
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_shouldAllowOwnerToDeleteOwnReview() {
        // given
        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review)); // review.user = user
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));   // role USER

        // when
        reviewService.deleteReview(review.getId(), user.getEmail());

        // then
        verify(reviewRepository).findById(review.getId());
        verify(userRepository).findByEmail(user.getEmail());
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_shouldThrowForbidden_whenOtherUserTriesToDelete() {
        // given
        User other = new User();
        other.setId(5L);
        other.setEmail("other@test.com");
        other.setRole(Role.USER);

        when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));  // review.user = user
        when(userRepository.findByEmail(other.getEmail()))
                .thenReturn(Optional.of(other));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reviewService.deleteReview(review.getId(), other.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).contains("You are not authorized");
        verify(reviewRepository).findById(review.getId());
        verify(userRepository).findByEmail(other.getEmail());
        verify(reviewRepository, never()).delete(any());
    }
}
