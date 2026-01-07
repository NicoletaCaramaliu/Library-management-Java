package com.example.library.service;

import com.example.library.model.Loan;
import com.example.library.model.Notification;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.NotificationRepository;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               LoanRepository loanRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getNotificationsForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return getNotificationsForUser(user.getId());
    }

    public List<Notification> getUnreadNotificationsForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return notificationRepository.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(user.getId());
    }

    public Notification markAsRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        // securitate simplă: userul poate marca doar notificările lui
        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to modify this notification");
        }

        if (!notification.isReadFlag()) {
            notification.setReadFlag(true);
            notification = notificationRepository.save(notification);
        }

        return notification;
    }

    public void deleteNotification(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to delete this notification");
        }

        notificationRepository.delete(notification);
    }
    //notificare generica
    public Notification createNotification(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    // creare notificare pt overdue loan
    public Notification createLoanNotification(Loan loan, String message) {
        Notification n = new Notification();
        n.setUser(loan.getUser());
        n.setLoan(loan);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    public void notifyLibrariansAboutOverdueLoansMoreThanWeekManual() {

        LocalDate today = LocalDate.now();
        LocalDate limit = today.minusWeeks(1);

        // imprumuturi overdue cu mai mult de o saptamana
        List<Loan> overdueLoans =
                loanRepository.findByDueDateBeforeAndReturnDateIsNull(limit);

        if (overdueLoans.isEmpty()) {
            return;
        }

        String message = "Exista " + overdueLoans.size()
                + " imprumuturi intarziate cu mai mult de o saptamana.";

        List<User> librarians = userRepository.findByRole(Role.LIBRARIAN);

        for (User librarian : librarians) {
            Notification n = new Notification();
            n.setUser(librarian);
            n.setMessage(message);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

}
