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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public ResponseEntity<Map<String, Object>> getAllJobs(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
        if (pageable.getPageSize() > 50) {
            throw new IllegalArgumentException("한 번에 최대 50개까지만 조회 가능합니다.");
        }
        
        Page<JobPosting> jobs = jobPostingService.findAll(pageable);
        List<JobResponse> dtoList = jobs.getContent().stream()
                .map(JobResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("data", Map.of("content", dtoList)));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable("id") Long id) {
        return jobPostingService.findById(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(job))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/url")
    public ResponseEntity<ApiResponse<?>> registerUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        long startTime = System.currentTimeMillis();
        
        Source source = new Source();
        source.setType("URL");
        source.setReferenceUri(url);
        source.setStatus("PROCESSING");
        source = sourceRepository.save(source);

        try {
            // 1. URL 스크래핑
            String rawText = urlScrapingService.scrapeUrl(url);

            // 2. AI 추출 엔진 실행
            List<Map<String, String>> extractedData = extractionEngine.extractJobPostings(rawText);

            if (extractedData.isEmpty()) {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", "AI_EMPTY_RESPONSE", startTime, "No job postings extracted from URL");
                return ResponseEntity.ok(new ApiResponse<>("error", "No data extracted from URL"));
            }

            // 3. ExtractionLog 저장
            saveExtractionLog(source, "gpt-4o", extractedData.toString(), startTime, null);

            // 4. JobPosting 저장 (첫 번째 항목 반환)
            JobResponse firstSavedDto = null;
            for (Map<String, String> data : extractedData) {
                JobPosting posting = new JobPosting();
                posting.setCompanyName(data.getOrDefault("companyName", null));
                posting.setLocation(data.getOrDefault("location", null));
                posting.setPosition(data.getOrDefault("position", "Unknown"));
                posting.setRequirements(data.getOrDefault("requirements", null));
                
                // 날짜 처리: AI가 준 데이터가 있으면 파싱, 없으면 null 유지
                if (data.get("openingDate") != null && !data.get("openingDate").equalsIgnoreCase("null")) {
                    try {
                        posting.setOpeningDate(LocalDate.parse(data.get("openingDate")));
                    } catch (Exception e) {
                        log.warn("Failed to parse openingDate: {}", data.get("openingDate"));
                    }
                }
                
                if (data.get("closingDate") != null && !data.get("closingDate").equalsIgnoreCase("null")) {
                    try {
                        posting.setClosingDate(LocalDate.parse(data.get("closingDate")));
                    } catch (Exception e) {
                        log.warn("Failed to parse closingDate: {}", data.get("closingDate"));
                    }
                }
                
                posting.setApplicationMethod(data.getOrDefault("applicationMethod", null));
                posting.setExpectedSalary(data.getOrDefault("expectedSalary", null));
                posting.setStatus("PENDING");
                posting.setSource(source);

                JobPosting saved = jobPostingService.save(posting);
                if (firstSavedDto == null) firstSavedDto = JobResponse.fromEntity(saved);
            }

            updateSource(source, "SUCCESS");
            return ResponseEntity.ok(ApiResponse.success(firstSavedDto));

        } catch (Exception e) {
            log.error("Error during URL registration: {}", e.getMessage(), e);
            updateSource(source, "FAILED");
            saveExtractionLog(source, "gpt-4o", "ERROR", startTime, e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Failed to process URL: " + e.getMessage()));
        }
    }

    @PutMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(@PathVariable Long id, @jakarta.validation.Valid @RequestBody com.hiresync.models.JobPostingRequest request) {
        return jobPostingService.update(id, request)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(JobResponse.fromEntity(updated))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobPostingService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
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
}
