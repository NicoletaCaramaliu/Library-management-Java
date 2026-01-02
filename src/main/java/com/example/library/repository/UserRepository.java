package com.example.library.repository;

import com.example.library.model.Role;
import com.example.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByActiveTrue();

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);
}

