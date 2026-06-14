package com.hiresync.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sources")
@Data
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String type; // PDF, URL, EMAIL
    
    @Column(name = "reference_uri", nullable = false, length = 500)
    private String referenceUri; // File path, URL, or Email ID
    
    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, PROCESSING
    
    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;
    
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
