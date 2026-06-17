package com.hiresync.controllers;

import com.hiresync.extraction.OpenAiExtractionEngine;
import com.hiresync.models.ApiResponse;
import com.hiresync.models.ExtractionLog;
import com.hiresync.models.JobPosting;
import com.hiresync.models.JobResponse;
import com.hiresync.models.Source;
import com.hiresync.repositories.ExtractionLogRepository;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
import com.hiresync.services.SaraminCrawlerService;
import com.hiresync.services.UrlScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@org.springframework.validation.annotation.Validated
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final UrlScrapingService urlScrapingService;
    private final OpenAiExtractionEngine extractionEngine;
    private final SourceRepository sourceRepository;
    private final ExtractionLogRepository extractionLogRepository;
    private final SaraminCrawlerService saraminCrawlerService;

    @PostMapping("/healthcheck")
    public ResponseEntity<ApiResponse<Void>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/crawl")
    public ResponseEntity<ApiResponse<String>> crawlUrl(@RequestParam String url) {
        log.info("Crawling URL: {}", url);
        String extractedText = saraminCrawlerService.crawlJobPosting(url);
        return ResponseEntity.ok(ApiResponse.success(extractedText));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllJobs(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
        if (pageable.getPageSize() > 50) {
            throw new IllegalArgumentException("한 번에 최대 50개까지만 조회 가능합니다.");
        }
        
        Page<JobPosting> jobs;
        if (status != null && !status.isBlank()) {
            jobs = jobPostingService.findByStatus(status, pageable);
        } else {
            jobs = jobPostingService.findAll(pageable);
        }
        
        List<JobResponse> dtoList = jobs.getContent().stream()
                .map(JobResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("content", dtoList);
        data.put("totalElements", jobs.getTotalElements());
        data.put("totalPages", jobs.getTotalPages());
        data.put("number", jobs.getNumber());
        data.put("size", jobs.getSize());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable("id") Long id) {
        return jobPostingService.findById(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(job))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sources/url")
    public ResponseEntity<ApiResponse<?>> registerUrl(@RequestBody Map<String, String> request) {
        if (request == null || request.get("url") == null || request.get("url").isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        
        String url = request.get("url");
        long startTime = System.currentTimeMillis();
        
        Source source = new Source();
        source.setType("URL");
        source.setReferenceUri(url);
        source.setStatus("PROCESSING");
        source = sourceRepository.save(source);

        try {
            // 1. URL 스크래핑 (사람인 URL인 경우 전용 크롤러 사용 시도)
            String rawText;
            if (url.contains("saramin.co.kr")) {
                log.info("Using SaraminCrawlerService for URL: {}", url);
                rawText = saraminCrawlerService.crawlJobPosting(url);
                // 크롤링 실패 시 기본 스크래퍼로 폴백
                if (rawText == null || rawText.isBlank() || rawText.startsWith("크롤링 중 오류 발생")) {
                    log.warn("SaraminCrawlerService failed or returned error. Falling back to UrlScrapingService.");
                    rawText = urlScrapingService.scrapeUrl(url);
                }
            } else {
                rawText = urlScrapingService.scrapeUrl(url);
            }

            // 2. AI 추출 엔진 실행
            List<Map<String, String>> extractedData = extractionEngine.extractJobPostings(rawText);

            if (extractedData.isEmpty()) {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", "AI_EMPTY_RESPONSE", startTime, "No job postings extracted from URL");
                return ResponseEntity.ok(ApiResponse.success(List.of()));
            }

            // 3. ExtractionLog 저장
            saveExtractionLog(source, "gpt-4o", extractedData.toString(), startTime, null);

            // 4. Source 상태를 SUCCESS로 먼저 업데이트 (DTO에 반영되도록)
            updateSource(source, "SUCCESS");

            // 5. JobPosting 저장 및 결과 리스트 생성
            List<JobResponse> savedDtos = new java.util.ArrayList<>();
            for (Map<String, String> data : extractedData) {
                JobPosting posting = new JobPosting();
                
                String companyName = safeGet(data, "companyName");
                posting.setCompanyName(companyName != null && !companyName.isBlank() ? companyName : "Unknown Company");
                
                posting.setLocation(safeGet(data, "location"));
                posting.setPosition(data.getOrDefault("position", "Unknown"));
                posting.setRequirements(safeGet(data, "requirements"));
                
                // 날짜 처리
                String openingDateStr = safeGet(data, "openingDate");
                if (openingDateStr != null && !openingDateStr.equalsIgnoreCase("null") && !openingDateStr.isBlank()) {
                    try {
                        posting.setOpeningDate(LocalDate.parse(openingDateStr));
                    } catch (Exception e) {
                        log.warn("Failed to parse openingDate: {}", openingDateStr);
                    }
                }
                
                String closingDateStr = safeGet(data, "closingDate");
                if (closingDateStr != null && !closingDateStr.equalsIgnoreCase("null") && !closingDateStr.isBlank()) {
                    try {
                        posting.setClosingDate(LocalDate.parse(closingDateStr));
                    } catch (Exception e) {
                        log.warn("Failed to parse closingDate: {}", closingDateStr);
                    }
                }
                
                posting.setApplicationMethod(safeGet(data, "applicationMethod"));
                posting.setExpectedSalary(safeGet(data, "expectedSalary"));
                posting.setStatus("PENDING");
                posting.setSource(source);

                JobPosting saved = jobPostingService.save(posting);
                savedDtos.add(JobResponse.fromEntity(saved));
            }

            return ResponseEntity.ok(ApiResponse.success(savedDtos));

        } catch (IllegalArgumentException e) {
            log.warn("Client error during URL registration: {}", e.getMessage());
            updateSource(source, "FAILED");
            saveExtractionLog(source, "gpt-4o", "CLIENT_ERROR", startTime, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        } catch (Exception e) {
            log.error("Unexpected error during URL registration: {}", e.getMessage(), e);
            updateSource(source, "FAILED");
            saveExtractionLog(source, "gpt-4o", "ERROR", startTime, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
    }

    @PostMapping("/jobs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@jakarta.validation.Valid @RequestBody com.hiresync.models.JobPostingRequest request) {
        JobPosting posting = new JobPosting();
        posting.setCompanyName(request.getCompanyName());
        posting.setLocation(request.getLocation());
        posting.setPosition(request.getPosition() != null ? request.getPosition() : "Unknown");
        posting.setRequirements(request.getRequirements());
        
        if (request.getOpeningDate() != null && !request.getOpeningDate().isBlank()) {
            try { posting.setOpeningDate(LocalDate.parse(request.getOpeningDate())); } catch (Exception ignored) {}
        }
        if (request.getClosingDate() != null && !request.getClosingDate().isBlank()) {
            try { posting.setClosingDate(LocalDate.parse(request.getClosingDate())); } catch (Exception ignored) {}
        }
        
        posting.setApplicationMethod(request.getApplicationMethod());
        posting.setExpectedSalary(request.getExpectedSalary());
        posting.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");

        JobPosting saved = jobPostingService.save(posting);
        return ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(saved)));
    }

    @PutMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(@PathVariable("id") Long id, @jakarta.validation.Valid @RequestBody com.hiresync.models.JobPostingRequest request) {
        return jobPostingService.update(id, request)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(updated))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable("id") Long id) {
        jobPostingService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String safeGet(Map<String, ?> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        return String.valueOf(value);
    }

    private void updateSource(Source s, String status) {
        s.setStatus(status);
        sourceRepository.save(s);
    }

    private void saveExtractionLog(Source source, String model, String data, long startTime, String error) {
        ExtractionLog logEntry = new ExtractionLog();
        logEntry.setSource(source);
        logEntry.setModelVersion(model);
        logEntry.setRawData(data);
        logEntry.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));
        logEntry.setErrorMessage(error);
        logEntry.setProcessedAt(LocalDateTime.now());
        extractionLogRepository.save(logEntry);
    }

    @PatchMapping("/jobs/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJobStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("상태 값은 필수입니다.");
        }
        return jobPostingService.updateStatus(id, status)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(updated))))
                .orElse(ResponseEntity.notFound().build());
    }
}
