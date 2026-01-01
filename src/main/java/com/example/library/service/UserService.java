package com.example.library.service;

import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User createUser(User user) {
        user.setId(null);
        user.setActive(true);

        user.setRole(Role.USER);

        String encoded = passwordEncoder.encode(user.getPassword());
        user.setPassword(encoded);

        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(updatedUser.getPassword());
            existing.setPassword(encoded);
        }

        existing.setRole(updatedUser.getRole());

        return userRepository.save(existing);
    }

    public void deactivateUser(Long id) {
        User existing = getUserById(id);
        existing.setActive(false);
        userRepository.save(existing);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // update pentru userul curent
    public User updateCurrentUser(String email, User updatedData) {
        User existing = getUserByEmail(email);

        existing.setName(updatedData.getName());
        existing.setEmail(updatedData.getEmail());

        if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(updatedData.getPassword());
            existing.setPassword(encoded);
        }


        return userRepository.save(existing);
    }
}
