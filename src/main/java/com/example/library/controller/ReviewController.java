package com.example.library.controller;

import com.example.library.model.Review;
import com.example.library.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody @Valid Review review,
                               Authentication authentication) {
        String email = authentication.getName();
        return reviewService.createReview(email, review);
    }

    // GET /api/reviews
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }


    // GET /api/reviews/book/3
    @GetMapping("/book/{bookId}")
    public List<Review> getReviewsForBook(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    // GET /api/reviews/me
    @GetMapping("/me")
    public List<Review> getMyReviews(Authentication authentication) {
        String email = authentication.getName();
        return reviewService.getReviewsForUserEmail(email);
    }

     //GET /api/reviews/book/3/average-rating
    @GetMapping("/book/{bookId}/average-rating")
    public Double getAverageRating(@PathVariable Long bookId) {
        return reviewService.getAverageRatingForBook(bookId);
    }

    // DELETE /api/reviews/{id}
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id,
                             Authentication authentication) {
        String email = authentication.getName();
        reviewService.deleteReview(id, email);
    }
}
