package com.hiresync.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    
    @NotBlank(message = "회사명은 필수입니다.")
    @Column(name = "company_name", length = 100)
    private String companyName;
    
    @Column(name = "location")
    private String location;
    
    @NotBlank(message = "채용 포지션은 필수입니다.")
    @Column(name = "position", length = 100, nullable = false)
    private String position;
    
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;
    
    @Column(name = "opening_date")
    private LocalDate openingDate;
    
    @Column(name = "closing_date")
    private LocalDate closingDate;
    
    @Column(name = "application_method")
    private String applicationMethod;
    
    @Column(name = "expected_salary", length = 100)
    private String expectedSalary;
    
    @NotBlank(message = "상태값은 필수입니다.")
    @Column(name = "status", length = 20, nullable = false)
    private String status; // DRAFT, PUBLISHED, EXPIRED
    
    @Version
    private Long version;
    
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
