package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.model.User;
import com.example.library.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Review buildReview(Long id, Long userId, Long bookId, int rating, String comment) {
        User user = new User();
        user.setId(userId);
        user.setEmail("user@mail.com");
        user.setName("User Test");
        user.setPassword("123456");

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Book Test");

        Review r = new Review();
        r.setId(id);
        r.setUser(user);
        r.setBook(book);
        r.setRating(rating);
        r.setComment(comment);
        r.setCreatedAt(LocalDateTime.now());

        return r;
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("POST /api/reviews - creează o recenzie")
    void createReview_shouldCreate() throws Exception {
        Review request = buildReview(null, 1L, 2L, 5, "Great book!");
        Review saved = buildReview(1L, 1L, 2L, 5, "Great book!");

        Mockito.when(reviewService.createReview(eq("user@mail.com"), any(Review.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Great book!")));
    }


    @Test
    @WithMockUser
    @DisplayName("GET /api/reviews")
    void getAllReviews_shouldReturnList() throws Exception {
        List<Review> list = Arrays.asList(
                buildReview(1L, 1L, 2L, 4, "Nice"),
                buildReview(2L, 2L, 2L, 5, "Excellent")
        );

        Mockito.when(reviewService.getAllReviews()).thenReturn(list);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/reviews/book/{id}")
    void getReviewsForBook_shouldReturnList() throws Exception {
        List<Review> list = List.of(buildReview(1L, 1L, 10L, 3, "Ok"));

        Mockito.when(reviewService.getReviewsForBook(10L)).thenReturn(list);

        mockMvc.perform(get("/api/reviews/book/{bookId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating", is(3)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("GET /api/reviews/me ")
    void getMyReviews_shouldReturnList() throws Exception {
        List<Review> list = List.of(
                buildReview(1L, 5L, 2L, 5, "Super")
        );

        Mockito.when(reviewService.getReviewsForUserEmail("user@mail.com"))
                .thenReturn(list);

        mockMvc.perform(get("/api/reviews/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/reviews/book/{id}/average-rating ")
    void getAverageRating_shouldReturnValue() throws Exception {
        Mockito.when(reviewService.getAverageRatingForBook(10L))
                .thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/book/{bookId}/average-rating", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("DELETE /api/reviews/{id} ")
    void deleteReview_shouldDelete() throws Exception {
        Mockito.doNothing().when(reviewService).deleteReview(1L, "user@mail.com");

        mockMvc.perform(delete("/api/reviews/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(reviewService)
                .deleteReview(1L, "user@mail.com");
    }
}
