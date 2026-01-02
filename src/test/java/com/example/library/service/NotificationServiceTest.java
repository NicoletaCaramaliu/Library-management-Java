package com.example.library.service;

import com.example.library.model.Loan;
import com.example.library.model.Notification;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.NotificationRepository;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private User librarian;
    private Notification notification;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("User");
        user.setRole(Role.USER);

        librarian = new User();
        librarian.setId(2L);
        librarian.setEmail("lib@test.com");
        librarian.setRole(Role.LIBRARIAN);

        loan = new Loan();
        loan.setId(50L);
        loan.setUser(user);
        loan.setDueDate(LocalDate.now().minusDays(10));

        notification = new Notification();
        notification.setId(100L);
        notification.setUser(user);
        notification.setMessage("Test message");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadFlag(false);
    }


    @Test
    void getNotificationsForUser_shouldReturnList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotificationsForUser(user.getId());

        assertThat(result).containsExactly(notification);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
    }


    @Test
    void getNotificationsForUserEmail_shouldFetchUserThenNotifications() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getNotificationsForUserEmail(user.getEmail());

        assertThat(result).containsExactly(notification);
        verify(userRepository).findByEmail(user.getEmail());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Test
    void getNotificationsForUserEmail_shouldThrow_whenUserMissing() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> notificationService.getNotificationsForUserEmail(user.getEmail())
        );

        assertThat(ex.getMessage()).contains("User not found with email");
        verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong());
    }


    @Test
    void getUnreadNotificationsForUserEmail_shouldReturnUnread() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));

        List<Notification> result =
                notificationService.getUnreadNotificationsForUserEmail(user.getEmail());

        assertThat(result).containsExactly(notification);
        verify(notificationRepository)
                .findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(user.getId());
    }


    @Test
    void markAsRead_shouldSetFlagTrue_andSave() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result =
                notificationService.markAsRead(notification.getId(), user.getEmail());

        assertThat(result.isReadFlag()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldThrow_whenNotOwner() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> notificationService.markAsRead(notification.getId(), "other@test.com")
        );

        assertThat(ex.getMessage()).contains("not allowed");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void deleteNotification_shouldDelete_whenOwner() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notification.getId(), user.getEmail());

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_shouldThrow_whenNotOwner() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> notificationService.deleteNotification(notification.getId(), "x@test.com")
        );

        assertThat(ex.getMessage()).contains("not allowed");
        verify(notificationRepository, never()).delete(any());
    }


    @Test
    void createNotification_shouldCreateForUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification n = invocation.getArgument(0);
                    n.setId(500L);
                    return n;
                });

        Notification result =
                notificationService.createNotification(user.getId(), "hello");

        assertThat(result.getId()).isEqualTo(500L);
        assertThat(result.getMessage()).isEqualTo("hello");
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(notificationRepository).save(any(Notification.class));
    }


    @Test
    void createLoanNotification_shouldAttachLoanAndUser() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification n =
                notificationService.createLoanNotification(loan, "overdue!");

        assertThat(n.getLoan()).isEqualTo(loan);
        assertThat(n.getUser()).isEqualTo(user);
        assertThat(n.getMessage()).isEqualTo("overdue!");
        assertThat(n.getCreatedAt()).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
    }


    @Test
    void notifyLibrarians_shouldCreateOneNotificationPerLibrarian_whenOverdueExists() {
        LocalDate limit = LocalDate.now().minusWeeks(1);

        when(loanRepository.findByDueDateBeforeAndReturnDateIsNull(limit))
                .thenReturn(List.of(loan));
        when(userRepository.findByRole(Role.LIBRARIAN))
                .thenReturn(List.of(librarian));

        notificationService.notifyLibrariansAboutOverdueLoansMoreThanWeekManual();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void notifyLibrarians_shouldDoNothing_whenNoOverdueLoans() {
        LocalDate limit = LocalDate.now().minusWeeks(1);

        when(loanRepository.findByDueDateBeforeAndReturnDateIsNull(limit))
                .thenReturn(List.of());

        notificationService.notifyLibrariansAboutOverdueLoansMoreThanWeekManual();

        verify(notificationRepository, never()).save(any());
        verify(userRepository, never()).findByRole(any());
    }
}
