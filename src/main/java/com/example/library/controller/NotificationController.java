package com.example.library.controller;

import com.example.library.model.Notification;
import com.example.library.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /api/notifications/me
    @GetMapping("/me")
    public List<Notification> getMyNotifications(Authentication authentication) {
        String email = authentication.getName();
        return notificationService.getNotificationsForUserEmail(email);
    }

    // GET /api/notifications/me/unread
    @GetMapping("/me/unread")
    public List<Notification> getMyUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        return notificationService.getUnreadNotificationsForUserEmail(email);
    }

    // POST /api/notifications/{id}/read
    @PostMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id,
                                   Authentication authentication) {
        String email = authentication.getName();
        return notificationService.markAsRead(id, email);
    }

    // DELETE /api/notifications/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyNotification(@PathVariable Long id,
                                     Authentication authentication) {
        String email = authentication.getName();
        notificationService.deleteNotification(id, email);
    }

    // POST /api/notifications/overdue-alert for librarian
    @PostMapping("/overdue-alert")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createOverdueAlertForLibrarians() {
        notificationService.notifyLibrariansAboutOverdueLoansMoreThanWeekManual();
    }

}
