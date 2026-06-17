package com.hiresync.services;

import com.hiresync.models.User;
import com.hiresync.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_WhenRoleIsNull_ShouldSetDefaultRole() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(null);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User registeredUser = userService.registerUser(user);

        // Then
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals("ACTIVE", registeredUser.getStatus());
        assertEquals("GENERAL", registeredUser.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void registerUser_WhenRoleIsNotNull_ShouldKeepRole() {
        // Given
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("adminPass");
        user.setRole("ADMIN");

        when(passwordEncoder.encode("adminPass")).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User registeredUser = userService.registerUser(user);

        // Then
        assertEquals("encodedAdminPass", registeredUser.getPassword());
        assertEquals("ACTIVE", registeredUser.getStatus());
        assertEquals("ADMIN", registeredUser.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        // Given
        String email = "find@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUser = userService.findByEmail(email);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ReturnsEmpty() {
        // Given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userService.findByEmail(email);

        // Then
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void registerUser_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        // Given
        User user = new User();
        user.setEmail("duplicate@example.com");
        user.setPassword("password123");

        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(new User()));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });
        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenEmailAlreadyExistsForAnotherUser_ShouldThrowIllegalArgumentException() {
        // Given
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("original@example.com");

        User userDetails = new User();
        userDetails.setEmail("taken@example.com"); // Changing to a taken email

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, userDetails);
        });
        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenEmailIsSame_ShouldNotThrowException() {
        // Given
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("same@example.com");
        existingUser.setName("Old Name");

        User userDetails = new User();
        userDetails.setEmail("same@example.com"); // Keeping the same email
        userDetails.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(userId, userDetails);

        // Then
        assertEquals("New Name", updatedUser.getName());
        assertEquals("same@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }
}
