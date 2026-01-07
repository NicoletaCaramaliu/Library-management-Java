package com.example.library.controller;

import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User buildUser(Long id, String name, String email, boolean active) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("123456");
        u.setRole(Role.USER);
        u.setActive(active);
        return u;
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/active")
    void getAllActive_shouldReturnActiveUsers() throws Exception {
        List<User> users = Arrays.asList(
                buildUser(1L, "Ana", "ana@mail.com", true),
                buildUser(2L, "Mihai", "mihai@mail.com", true)
        );

        Mockito.when(userService.getAllActiveUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/all")
    void getAll_shouldReturnAllUsers() throws Exception {
        List<User> users = List.of(buildUser(1L, "Ana", "ana@mail.com", true));

        Mockito.when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is("ana@mail.com")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/{id}")
    void getById_shouldReturnUser() throws Exception {
        User user = buildUser(1L, "Ana", "ana@mail.com", true);
        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Ana")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users ")
    void create_shouldCreateUser() throws Exception {
        User request = buildUser(null, "Ana", "ana@mail.com", true);
        User saved = buildUser(1L, "Ana", "ana@mail.com", true);

        Mockito.when(userService.createUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id} ")
    void update_shouldUpdateUser() throws Exception {
        User request = buildUser(null, "Ana Updated", "ana@mail.com", true);
        User updated = buildUser(1L, "Ana Updated", "ana@mail.com", true);

        Mockito.when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Ana Updated")));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/users/{id} ")
    void deactivate_shouldDeactivateUser() throws Exception {
        Mockito.doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deactivateUser(1L);
    }

    @Test
    @WithMockUser(username = "ana@mail.com")
    @DisplayName("GET /api/users/me ")
    void getCurrentUser_shouldReturnLoggedUser() throws Exception {
        User user = buildUser(1L, "Ana", "ana@mail.com", true);

        Mockito.when(userService.getUserByEmail("ana@mail.com"))
                .thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("ana@mail.com")));
    }

    @Test
    @WithMockUser(username = "ana@mail.com")
    @DisplayName("PUT /api/users/me ")
    void updateCurrentUser_shouldUpdateProfile() throws Exception {
        User request = buildUser(null, "Ana Noua", "ana@mail.com", true);
        User updated = buildUser(1L, "Ana Noua", "ana@mail.com", true);

        Mockito.when(userService.updateCurrentUser(eq("ana@mail.com"), any(User.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Ana Noua")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/{id}/activate ")
    void activate_shouldActivateUser() throws Exception {
        User activated = buildUser(1L, "Ana", "ana@mail.com", true);

        Mockito.when(userService.activateUser(1L)).thenReturn(activated);

        mockMvc.perform(put("/api/users/{id}/activate", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }
}
