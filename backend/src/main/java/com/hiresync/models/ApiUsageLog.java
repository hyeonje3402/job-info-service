package com.hiresync.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_usage_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
