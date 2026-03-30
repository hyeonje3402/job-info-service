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
    
    @Column(name = "source_type", nullable = false)
    private String sourceType;
    
    @Column(name = "source_origin", length = 500)
    private String sourceOrigin;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "extracted_data", columnDefinition = "JSON")
    private String extractedData;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onProcess() {
        processedAt = LocalDateTime.now();
    }
}
