package com.hiresync.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "extraction_logs")
@Data
public class ExtractionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;
    
    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;
    
    @Column(name = "raw_data", columnDefinition = "TEXT", nullable = false)
    private String rawData; // JSON formatted extracted data
    
    @Column(name = "processing_time_ms", nullable = false)
    private Integer processingTimeMs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
