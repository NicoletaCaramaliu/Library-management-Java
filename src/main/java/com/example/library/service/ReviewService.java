package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }


    public Review createReview(String userEmail, Review reviewInput) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(
                        "User not found with email: " + userEmail,
                        HttpStatus.NOT_FOUND
                ));

        if (reviewInput.getBook() == null || reviewInput.getBook().getId() == null) {
            throw new BusinessException("Book id is required", HttpStatus.BAD_REQUEST);
        }

        Book book = bookRepository.findById(reviewInput.getBook().getId())
                .orElseThrow(() -> new BusinessException(
                        "Book not found with id: " + reviewInput.getBook().getId(),
                        HttpStatus.NOT_FOUND
                ));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(reviewInput.getRating());
        review.setComment(reviewInput.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }


    public List<Review> getReviewsForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "User not found with email: " + email,
                        HttpStatus.NOT_FOUND
                ));

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }


    public Double getAverageRatingForBook(Long bookId) {
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        return avg != null ? avg : 0.0;
    }

    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(
                        "Review not found with id: " + reviewId,
                        HttpStatus.NOT_FOUND
                ));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(
                        "User not found with email: " + userEmail,
                        HttpStatus.NOT_FOUND
                ));

        if (user.getRole() == Role.LIBRARIAN || user.getRole() == Role.ADMIN) {
            reviewRepository.delete(review);
            return;
        }

        if (!review.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(
                    "You are not authorized to delete this review",
                    HttpStatus.FORBIDDEN
            );
        }

        reviewRepository.delete(review);
    }

}
