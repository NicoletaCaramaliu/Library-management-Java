package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private User librarian;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("User One");
        user.setActive(true);
        user.setRole(Role.USER);

        librarian = new User();
        librarian.setId(2L);
        librarian.setEmail("lib@test.com");
        librarian.setName("Librarian");
        librarian.setActive(true);
        librarian.setRole(Role.LIBRARIAN);

        book = new Book();
        book.setId(10L);
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setAvailableCopies(3);

        loan = new Loan();
        loan.setId(100L);
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now().minusDays(5));
        loan.setDueDate(LocalDate.now().plusDays(9));
        loan.setReturnDate(null);
    }


    @Test
    void getAllLoans_shouldReturnList() {
        // given
        when(loanRepository.findAll()).thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getAllLoans();

        // then
        assertThat(result).containsExactly(loan);
        verify(loanRepository).findAll();
    }


    @Test
    void getLoanById_shouldReturnLoan_whenExists() {
        // given
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

        // when
        Loan result = loanService.getLoanById(100L);

        // then
        assertThat(result).isEqualTo(loan);
        verify(loanRepository).findById(100L);
    }

    @Test
    void getLoanById_shouldThrowRuntimeException_whenNotFound() {
        // given
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loanService.getLoanById(999L)
        );

        // then
        assertThat(ex.getMessage()).contains("Loan not found with id: 999");
        verify(loanRepository).findById(999L);
    }


    @Test
    void getLoansForUser_shouldUseRepository() {
        // given
        when(loanRepository.findByUserId(user.getId())).thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getLoansForUser(user.getId());

        // then
        assertThat(result).containsExactly(loan);
        verify(loanRepository).findByUserId(user.getId());
    }


    @Test
    void getOverdueLoans_shouldUseRepositoryWithTodayDate() {
        // given
        LocalDate today = LocalDate.now();
        when(loanRepository.findByDueDateBeforeAndReturnDateIsNull(today))
                .thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getOverdueLoans();

        // then
        assertThat(result).containsExactly(loan);
        verify(loanRepository).findByDueDateBeforeAndReturnDateIsNull(today);
    }


    @Test
    void createLoan_shouldCreateLoan_whenUserAndBookValidAndCopiesAvailable() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        // when
        Loan result = loanService.createLoan(user.getId(), book.getId());

        // then
        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getLoanDate()).isEqualTo(LocalDate.now());
        assertThat(result.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));

        // available copies scade cu 1
        assertThat(book.getAvailableCopies()).isEqualTo(2);
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoan_shouldThrow_whenUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.createLoan(1L, book.getId())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with id: 1");
        verify(userRepository).findById(1L);
        verifyNoInteractions(bookRepository, loanRepository);
    }

    @Test
    void createLoan_shouldThrow_whenBookNotFound() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.createLoan(user.getId(), book.getId())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Book not found with id");
        verify(userRepository).findById(user.getId());
        verify(bookRepository).findById(book.getId());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_shouldThrow_whenUserNotActive() {
        // given
        user.setActive(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.createLoan(user.getId(), book.getId())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("User is not active");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_shouldThrow_whenNoCopiesAvailable() {
        // given
        book.setAvailableCopies(0);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.createLoan(user.getId(), book.getId())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("No copies available");
        verify(loanRepository, never()).save(any());
    }


    @Test
    void returnLoan_shouldAllowOwnerToReturnAndIncreaseCopies() {
        // given
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int copiesBefore = book.getAvailableCopies();

        // when
        Loan result = loanService.returnLoan(loan.getId(), user.getEmail());

        // then
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(book.getAvailableCopies()).isEqualTo(copiesBefore + 1);

        verify(loanRepository).findById(loan.getId());
        verify(userRepository).findByEmail(user.getEmail());
        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnLoan_shouldThrow_whenAlreadyReturned() {
        // given
        loan.setReturnDate(LocalDate.now().minusDays(1));
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.returnLoan(loan.getId(), user.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Loan already returned");
        verify(userRepository, never()).findByEmail(anyString());
        verify(bookRepository, never()).save(any());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_shouldThrow_whenCurrentUserNotFound() {
        // given
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.returnLoan(loan.getId(), "missing@test.com")
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Current user not found");
        verify(bookRepository, never()).save(any());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_shouldAllowLibrarianToReturnOthersLoan() {
        // given
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail(librarian.getEmail())).thenReturn(Optional.of(librarian));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int copiesBefore = book.getAvailableCopies();

        // when
        Loan result = loanService.returnLoan(loan.getId(), librarian.getEmail());

        // then
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(book.getAvailableCopies()).isEqualTo(copiesBefore + 1);
        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnLoan_shouldThrow_whenUserNotOwnerOrStaff() {
        // given
        User other = new User();
        other.setId(5L);
        other.setEmail("other@test.com");
        other.setRole(Role.USER);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(userRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.returnLoan(loan.getId(), other.getEmail())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("You are not allowed to return this loan");
        verify(bookRepository, never()).save(any());
        verify(loanRepository, never()).save(any());
    }


    @Test
    void deleteLoan_shouldDelete_whenExists() {
        // given
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        // when
        loanService.deleteLoan(loan.getId());

        // then
        verify(loanRepository).findById(loan.getId());
        verify(loanRepository).delete(loan);
    }


    @Test
    void createLoanForUserEmail_shouldFindUserAndDelegateToCreateLoan() {
        // given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setId(300L);
            return saved;
        });

        // when
        Loan result = loanService.createLoanForUserEmail(user.getEmail(), book.getId());

        // then
        assertThat(result.getId()).isEqualTo(300L);
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).findById(user.getId());
        verify(bookRepository).findById(book.getId());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoanForUserEmail_shouldThrow_whenUserNotFoundByEmail() {
        // given
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> loanService.createLoanForUserEmail("missing@test.com", book.getId())
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with email");
        verifyNoInteractions(bookRepository, loanRepository);
    }


    @Test
    void getLoansForUserEmail_shouldReturnLoans() {
        // given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(loanRepository.findByUserId(user.getId())).thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getLoansForUserEmail(user.getEmail());

        // then
        assertThat(result).containsExactly(loan);
        verify(userRepository).findByEmail(user.getEmail());
        verify(loanRepository).findByUserId(user.getId());
    }

    @Test
    void getActiveLoansForUserEmail_shouldReturnActiveLoans() {
        // given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(loanRepository.findByUserIdAndReturnDateIsNull(user.getId())).thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getActiveLoansForUserEmail(user.getEmail());

        // then
        assertThat(result).containsExactly(loan);
        verify(userRepository).findByEmail(user.getEmail());
        verify(loanRepository).findByUserIdAndReturnDateIsNull(user.getId());
    }

    @Test
    void getAllActiveLoans_shouldUseRepository() {
        // given
        when(loanRepository.findByReturnDateIsNull()).thenReturn(List.of(loan));

        // when
        List<Loan> result = loanService.getAllActiveLoans();

        // then
        assertThat(result).containsExactly(loan);
        verify(loanRepository).findByReturnDateIsNull();
    }


    @Test
    void createOverdueNotifications_shouldCreateNotificationForEachOverdueLoan() {
        // given
        when(loanRepository.findByDueDateBeforeAndReturnDateIsNull(any(LocalDate.class)))
                .thenReturn(List.of(loan));

        // when
        int count = loanService.createOverdueNotifications();

        // then
        assertThat(count).isEqualTo(1);
        verify(notificationService).createLoanNotification(eq(loan), anyString());
    }

    @Test
    void createOverdueNotifications_shouldReturnZero_whenNoOverdueLoans() {
        // given
        when(loanRepository.findByDueDateBeforeAndReturnDateIsNull(any(LocalDate.class)))
                .thenReturn(List.of());

        // when
        int count = loanService.createOverdueNotifications();

        // then
        assertThat(count).isEqualTo(0);
        verify(notificationService, never()).createLoanNotification(any(), anyString());
    }
}
