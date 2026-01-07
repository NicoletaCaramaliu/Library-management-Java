package com.example.library.controller;

import com.example.library.model.Notification;
import com.example.library.model.User;
import com.example.library.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Notification buildNotification(Long id, Long userId, boolean read) {
        User user = new User();
        user.setId(userId);
        user.setName("User " + userId);
        user.setEmail("user" + userId + "@mail.com");
        user.setPassword("123456");

        Notification n = new Notification();
        n.setId(id);
        n.setUser(user);
        n.setMessage("Message " + id);
        n.setCreatedAt(LocalDateTime.now().minusHours(2));
        n.setReadFlag(read);

        return n;
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("GET /api/notifications/me ")
    void getMyNotifications_shouldReturnNotifications() throws Exception {
        List<Notification> list = Arrays.asList(
                buildNotification(1L, 5L, false),
                buildNotification(2L, 5L, true)
        );

        Mockito.when(notificationService.getNotificationsForUserEmail("user@mail.com"))
                .thenReturn(list);

        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message", is("Message 1")));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("GET /api/notifications/me/unread ")
    void getMyUnreadNotifications_shouldReturnUnread() throws Exception {
        List<Notification> list = List.of(
                buildNotification(1L, 5L, false)
        );

        Mockito.when(notificationService.getUnreadNotificationsForUserEmail("user@mail.com"))
                .thenReturn(list);

        mockMvc.perform(get("/api/notifications/me/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].readFlag", is(false)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("POST /api/notifications/{id}/read ")
    void markAsRead_shouldUpdateNotification() throws Exception {
        Notification notif = buildNotification(1L, 5L, true);

        Mockito.when(notificationService.markAsRead(1L, "user@mail.com"))
                .thenReturn(notif);

        mockMvc.perform(post("/api/notifications/{id}/read", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.readFlag", is(true)));
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    @DisplayName("DELETE /api/notifications/{id}")
    void deleteMyNotification_shouldDelete() throws Exception {
        Mockito.doNothing().when(notificationService).deleteNotification(1L, "user@mail.com");

        mockMvc.perform(delete("/api/notifications/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(notificationService)
                .deleteNotification(1L, "user@mail.com");
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notifications/overdue-alert ")
    void createOverdueAlertForLibrarians_shouldTriggerService() throws Exception {

        mockMvc.perform(post("/api/notifications/overdue-alert")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(notificationService)
                .notifyLibrariansAboutOverdueLoansMoreThanWeekManual();
    }
}
