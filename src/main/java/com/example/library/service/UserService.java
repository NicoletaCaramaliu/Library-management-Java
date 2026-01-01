package com.example.library.service;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    // DI prin constructor
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // listare doar utilizatori activi
    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User createUser(User user) {
        user.setId(null);
        user.setActive(true);  //noul user e activ implicit
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());

        return userRepository.save(existing);
    }

    // dezactivare cont in loc de stergere
    public void deactivateUser(Long id) {
        User existing = getUserById(id);
        existing.setActive(false);
        userRepository.save(existing);
    }
}
