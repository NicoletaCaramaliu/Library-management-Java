package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.example.library.model.Role;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));
    }

    public List<Loan> getLoansForUser(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<Loan> getOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loanRepository.findByDueDateBeforeAndReturnDateIsNull(today);
    }

    public Loan createLoan(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for this book");
        }

        // scad un exemplar
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));

        return loanRepository.save(loan);
    }


    public Loan returnLoan(Long loanId, String currentUserEmail) {
        Loan loan = getLoanById(loanId);

        if (loan.getReturnDate() != null) {
            throw new RuntimeException("Loan already returned");
        }

        // cine este userul curent?
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + currentUserEmail));

        // daca este simplu USER, poate returna doar imprumuturile lui
        boolean isOwner = loan.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == Role.LIBRARIAN || currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isStaff) {
            throw new RuntimeException("You are not allowed to return this loan");
        }

        // logica de returnare
        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }


    public void deleteLoan(Long id) {
        Loan existing = getLoanById(id);
        loanRepository.delete(existing);
    }

    public Loan createLoanForUserEmail(String email, Long bookId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return createLoan(user.getId(), bookId);
    }

    public List<Loan> getLoansForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return getLoansForUser(user.getId());
    }

    public List<Loan> getActiveLoansForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return loanRepository.findByUserIdAndReturnDateIsNull(user.getId());
    }

    public List<Loan> getAllActiveLoans() {
        return loanRepository.findByReturnDateIsNull();
    }


}
