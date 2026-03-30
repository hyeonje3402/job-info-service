package com.hiresync.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Data
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "field", nullable = false)
    private String field;
    
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;
    
    @Column(name = "deadline")
    private String deadline;
    
    @Column(name = "method", columnDefinition = "TEXT")
    private String method;
    
    @Column(name = "expected_salary")
    private String expectedSalary;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private SourceType source;
    
    @Column(name = "source_origin", length = 500)
    private String sourceOrigin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SourceType {
        PDF, URL, EMAIL
    }

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
