package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.User;
import com.example.library.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    private Loan buildLoan(Long id, Long userId, Long bookId) {
        User user = new User();
        user.setId(userId);
        user.setName("User " + userId);
        user.setEmail("user" + userId + "@mail.com");
        user.setPassword("123456");

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Book " + bookId);
        book.setAuthor("Author " + bookId);
        book.setIsbn("ISBN-" + bookId);
        book.setPublishedYear(2020);
        book.setAvailableCopies(3);

        Loan loan = new Loan();
        loan.setId(id);
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now().minusDays(5));
        loan.setDueDate(LocalDate.now().plusDays(5));
        loan.setReturnDate(null);

        return loan;
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/loans ")
    void getAll_shouldReturnListOfLoans() throws Exception {
        List<Loan> loans = Arrays.asList(
                buildLoan(1L, 1L, 10L),
                buildLoan(2L, 2L, 20L)
        );
        Mockito.when(loanService.getAllLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/loans/{id}")
    void getById_shouldReturnLoan() throws Exception {
        Loan loan = buildLoan(1L, 1L, 10L);
        Mockito.when(loanService.getLoanById(1L)).thenReturn(loan);

        mockMvc.perform(get("/api/loans/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.book.id", is(10)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/loans/user/{userId} ")
    void getForUser_shouldReturnLoansForUser() throws Exception {
        List<Loan> loans = List.of(
                buildLoan(1L, 5L, 10L),
                buildLoan(2L, 5L, 11L)
        );
        Mockito.when(loanService.getLoansForUser(5L)).thenReturn(loans);

        mockMvc.perform(get("/api/loans/user/{userId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].user.id", is(5)))
                .andExpect(jsonPath("$[1].user.id", is(5)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/loans/overdue ")
    void getOverdue_shouldReturnOverdueLoans() throws Exception {
        List<Loan> loans = List.of(
                buildLoan(1L, 1L, 10L)
        );
        Mockito.when(loanService.getOverdueLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/loans?userId=&bookId=")
    void create_shouldCreateLoan() throws Exception {
        Loan created = buildLoan(1L, 3L, 7L);
        Mockito.when(loanService.createLoan(3L, 7L)).thenReturn(created);

        mockMvc.perform(post("/api/loans")
                        .with(csrf())
                        .param("userId", "3")
                        .param("bookId", "7"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.user.id", is(3)))
                .andExpect(jsonPath("$.book.id", is(7)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("POST /api/loans/{id}/return ")
    void returnLoan_shouldReturnLoan() throws Exception {
        Loan returned = buildLoan(1L, 1L, 10L);
        returned.setReturnDate(LocalDate.now());

        Mockito.when(loanService.returnLoan(1L, "user@mail.com"))
                .thenReturn(returned);

        mockMvc.perform(post("/api/loans/{id}/return", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/loans/{id}")
    void delete_shouldDeleteLoan() throws Exception {
        Mockito.doNothing().when(loanService).deleteLoan(1L);

        mockMvc.perform(delete("/api/loans/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(loanService).deleteLoan(1L);
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("POST /api/loans/borrow/{bookId} ")
    void borrowForCurrentUser_shouldCreateLoanForCurrentUser() throws Exception {
        Loan loan = buildLoan(1L, 5L, 10L);

        Mockito.when(loanService.createLoanForUserEmail("user@mail.com", 10L))
                .thenReturn(loan);

        mockMvc.perform(post("/api/loans/borrow/{bookId}", 10L)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.book.id", is(10)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("GET /api/loans/me ")
    void getMyLoans_shouldReturnLoansForCurrentUser() throws Exception {
        List<Loan> loans = List.of(
                buildLoan(1L, 5L, 10L),
                buildLoan(2L, 5L, 11L)
        );

        Mockito.when(loanService.getLoansForUserEmail("user@mail.com"))
                .thenReturn(loans);

        mockMvc.perform(get("/api/loans/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("GET /api/loans/me/active ")
    void getMyActiveLoans_shouldReturnActiveLoansForCurrentUser() throws Exception {
        List<Loan> loans = List.of(
                buildLoan(1L, 5L, 10L)
        );

        Mockito.when(loanService.getActiveLoansForUserEmail("user@mail.com"))
                .thenReturn(loans);

        mockMvc.perform(get("/api/loans/me/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/loans/allActive ")
    void getAllActiveLoans_shouldReturnAllActiveLoans() throws Exception {
        List<Loan> loans = List.of(
                buildLoan(1L, 1L, 10L),
                buildLoan(2L, 2L, 20L)
        );

        Mockito.when(loanService.getAllActiveLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans/allActive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/loans/overdue/notify ")
    void notifyOverdueLoans_shouldReturnNumberOfNotifications() throws Exception {
        Mockito.when(loanService.createOverdueNotifications()).thenReturn(3);

        mockMvc.perform(post("/api/loans/overdue/notify")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
