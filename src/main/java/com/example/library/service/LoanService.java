package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.library.model.Role;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
                .orElseThrow(() -> new BusinessException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException("Book not found with id: " + bookId, HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException("User is not active", HttpStatus.BAD_REQUEST);
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException("No copies available for this book", HttpStatus.BAD_REQUEST);
        }

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
            throw new BusinessException("Loan already returned", HttpStatus.BAD_REQUEST);
        }

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BusinessException("Current user not found: " + currentUserEmail, HttpStatus.NOT_FOUND));

        boolean isOwner = loan.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == Role.LIBRARIAN || currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isStaff) {
            throw new BusinessException("You are not allowed to return this loan", HttpStatus.BAD_REQUEST);
        }

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
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));

        return createLoan(user.getId(), bookId);
    }

    public List<Loan> getLoansForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));

        return getLoansForUser(user.getId());
    }

    public List<Loan> getActiveLoansForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));

        return loanRepository.findByUserIdAndReturnDateIsNull(user.getId());
    }

    public List<Loan> getAllActiveLoans() {
        return loanRepository.findByReturnDateIsNull();
    }

    public int createOverdueNotifications() {
        List<Loan> overdue = getOverdueLoans();
        int count = 0;
        for (Loan loan : overdue) {
            String msg = "Loan for book '" + loan.getBook().getTitle()
                    + "' is overdue. Due date was " + loan.getDueDate() + ".";
            notificationService.createLoanNotification(loan, msg);
            count++;
        }
        return count;
    }


}
