package com.hiresync.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Data
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;
    
    @Column(name = "location", nullable = false)
    private String location;
    
    @Column(name = "position", length = 100, nullable = false)
    private String position;
    
    @Column(name = "requirements", columnDefinition = "TEXT", nullable = false)
    private String requirements;
    
    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;
    
    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;
    
    @Column(name = "application_method", nullable = false)
    private String applicationMethod;
    
    @Column(name = "expected_salary", length = 100, nullable = false)
    private String expectedSalary;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status; // PENDING, APPROVED, EXPIRED
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

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
