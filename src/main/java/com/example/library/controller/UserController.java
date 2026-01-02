package com.example.library.controller;

import com.example.library.model.User;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    @GetMapping("/active")
    public List<User> getAllActive() {
        return userService.getAllActiveUsers();
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    //activate user
    @PutMapping("/{id}/activate")
    public User activate(@PathVariable Long id) {
        return userService.activateUser(id);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // POST /api/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // DELETE /api/users/{id} -> dezactivare nu stergere din DB
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
    }

    // GET /api/users/me - profilul utilizatorului logat
    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // username = email
        return userService.getUserByEmail(email);
    }

    // PUT /api/users/me - actualizare profil utiliz logat
    @PutMapping("/me")
    public User updateCurrentUser(@Valid @RequestBody User user, Authentication authentication) {
        String email = authentication.getName();
        return userService.updateCurrentUser(email, user);
    }

}
