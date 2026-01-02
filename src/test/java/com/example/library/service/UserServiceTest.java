package com.example.library.service;

import com.example.library.exception.BusinessException;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("one@test.com");
        user1.setPassword("pass1");
        user1.setActive(true);
        user1.setRole(Role.USER);

        user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("two@test.com");
        user2.setPassword("pass2");
        user2.setActive(false);
        user2.setRole(Role.LIBRARIAN);
    }

    @Test
    void getAllActiveUsers_shouldUseRepository() {
        // given
        when(userRepository.findByActiveTrue()).thenReturn(List.of(user1));

        // when
        List<User> result = userService.getAllActiveUsers();

        // then
        assertThat(result).containsExactly(user1);
        verify(userRepository).findByActiveTrue();
    }

    @Test
    void getAllUsers_shouldUseRepository() {
        // given
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // when
        List<User> result = userService.getAllUsers();

        // then
        assertThat(result).containsExactly(user1, user2);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // when
        User result = userService.getUserById(1L);

        // then
        assertThat(result).isEqualTo(user1);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.getUserById(99L)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with id: 99");
        verify(userRepository).findById(99L);
    }

    @Test
    void activateUser_shouldSetActiveTrueAndSave() {
        // given
        user1.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.activateUser(1L);

        // then
        assertThat(result.isActive()).isTrue();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user1);
    }

    @Test
    void createUser_shouldSetIdNullActiveTrueRoleUserAndEncodePassword() {
        // given
        User toCreate = new User();
        toCreate.setId(123L);
        toCreate.setName("New User");
        toCreate.setEmail("new@test.com");
        toCreate.setPassword("plain-pass");
        toCreate.setActive(false);
        toCreate.setRole(Role.ADMIN);

        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");

        User saved = new User();
        saved.setId(10L);
        saved.setName("New User");
        saved.setEmail("new@test.com");
        saved.setPassword("encoded-pass");
        saved.setActive(true);
        saved.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        // when
        User result = userService.createUser(toCreate);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPassword()).isEqualTo("encoded-pass");

        verify(passwordEncoder).encode("plain-pass");
        verify(userRepository).save(argThat(u ->
                u.getId() == null &&
                        u.isActive() &&
                        u.getRole() == Role.USER &&
                        "encoded-pass".equals(u.getPassword())
        ));
    }

    @Test
    void updateUser_shouldUpdateFieldsAndEncodePassword_whenPasswordProvided() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        User updated = new User();
        updated.setName("Updated Name");
        updated.setEmail("updated@test.com");
        updated.setPassword("new-pass");
        updated.setRole(Role.LIBRARIAN);

        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.updateUser(1L, updated);

        // then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-new-pass");
        assertThat(result.getRole()).isEqualTo(Role.LIBRARIAN);

        verify(passwordEncoder).encode("new-pass");
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_shouldNotChangePassword_whenPasswordNullOrBlank() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        String oldPassword = user1.getPassword();

        User updated = new User();
        updated.setName("Updated Name");
        updated.setEmail("updated@test.com");
        updated.setPassword("   "); // blank
        updated.setRole(Role.ADMIN);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.updateUser(1L, updated);

        // then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@test.com");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.getPassword()).isEqualTo(oldPassword);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_shouldThrow_whenNotFound() {
        // given
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        User updated = new User();
        updated.setName("X");

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updateUser(5L, updated)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).findById(5L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_shouldSetActiveFalseAndSave() {
        // given
        user1.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // when
        userService.deactivateUser(1L);

        // then
        assertThat(user1.isActive()).isFalse();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user1);
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenExists() {
        // given
        when(userRepository.findByEmail("one@test.com")).thenReturn(Optional.of(user1));

        // when
        User result = userService.getUserByEmail("one@test.com");

        // then
        assertThat(result).isEqualTo(user1);
        verify(userRepository).findByEmail("one@test.com");
    }

    @Test
    void getUserByEmail_shouldThrow_whenNotFound() {
        // given
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.getUserByEmail("missing@test.com")
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("User not found with email: missing@test.com");
        verify(userRepository).findByEmail("missing@test.com");
    }

    @Test
    void updateCurrentUser_shouldUpdateFieldsAndEncodePassword_whenProvided() {
        // given
        when(userRepository.findByEmail("one@test.com")).thenReturn(Optional.of(user1));

        User updated = new User();
        updated.setName("Current Updated");
        updated.setEmail("new-email@test.com");
        updated.setPassword("new-pass");

        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-current-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.updateCurrentUser("one@test.com", updated);

        // then
        assertThat(result.getName()).isEqualTo("Current Updated");
        assertThat(result.getEmail()).isEqualTo("new-email@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-current-pass");

        verify(passwordEncoder).encode("new-pass");
        verify(userRepository).save(user1);
    }

    @Test
    void updateCurrentUser_shouldNotChangePassword_whenBlankOrNull() {
        // given
        when(userRepository.findByEmail("one@test.com")).thenReturn(Optional.of(user1));
        String oldPassword = user1.getPassword();

        User updated = new User();
        updated.setName("Current Updated");
        updated.setEmail("new-email@test.com");
        updated.setPassword("   "); // blank

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.updateCurrentUser("one@test.com", updated);

        // then
        assertThat(result.getName()).isEqualTo("Current Updated");
        assertThat(result.getEmail()).isEqualTo("new-email@test.com");
        assertThat(result.getPassword()).isEqualTo(oldPassword);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user1);
    }

    @Test
    void updateCurrentUser_shouldThrow_whenUserNotFoundByEmail() {
        // given
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        User updated = new User();
        updated.setName("X");

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updateCurrentUser("missing@test.com", updated)
        );

        // then
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userRepository).findByEmail("missing@test.com");
        verify(userRepository, never()).save(any());
    }
}
