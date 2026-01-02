package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.http.HttpStatus;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User activateUser(Long id) {
        User existing = getUserById(id);
        existing.setActive(true);
        return userRepository.save(existing);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found with id: " + id, HttpStatus.NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));
    }

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
