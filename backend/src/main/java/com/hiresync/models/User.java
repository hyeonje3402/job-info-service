package com.hiresync.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiresync.security.EmailConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EmailConverter.class)
    @Column(nullable = false, unique = true, length = 255) // Increased length for encrypted value
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String role; // ADMIN, GENERAL

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE, REVOKED

    @Column(length = 50)
    private String phone;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
