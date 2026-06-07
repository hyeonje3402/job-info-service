package com.hiresync.services;

import com.hiresync.models.User;
import com.hiresync.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("ACTIVE");
        if (user.getRole() == null) {
            user.setRole("GENERAL");
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findUsers(String role, String status) {
        boolean hasRole = role != null && !role.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        
        if (hasRole && hasStatus) {
            return userRepository.findByRoleAndStatus(role, status);
        } else if (hasRole) {
            return userRepository.findByRole(role);
        } else if (hasStatus) {
            return userRepository.findByStatus(status);
        } else {
            return userRepository.findAll();
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());
        user.setPhone(userDetails.getPhone());
        user.setProfileImage(userDetails.getProfileImage());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(user);
    }
}
