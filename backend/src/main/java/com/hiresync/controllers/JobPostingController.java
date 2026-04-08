package com.hiresync.controllers;

import com.hiresync.extraction.OpenAiExtractionEngine;
import com.hiresync.models.ApiResponse;
import com.hiresync.models.ExtractionLog;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.ExtractionLogRepository;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final UrlScrapingService urlScrapingService;
    private final OpenAiExtractionEngine extractionEngine;
    private final SourceRepository sourceRepository;
    private final ExtractionLogRepository extractionLogRepository;

    @PostMapping("/healthcheck")
    public ResponseEntity<ApiResponse<Void>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<JobPosting>>> getAllJobs(Pageable pageable) {
        Page<JobPosting> jobs = jobPostingService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<JobPosting>> getJobById(@PathVariable Long id) {
        return jobPostingService.findById(id)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job)))
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
            JobPosting firstSaved = null;
            for (Map<String, String> data : extractedData) {
                JobPosting posting = new JobPosting();
                posting.setCompanyName(data.getOrDefault("companyName", "Unknown"));
                posting.setLocation(data.getOrDefault("location", "Unknown"));
                posting.setPosition(data.getOrDefault("position", "Unknown"));
                posting.setRequirements(data.getOrDefault("requirements", "N/A"));
                
                // 날짜 처리 (기본값 설정)
                posting.setOpeningDate(LocalDate.now());
                posting.setClosingDate(LocalDate.now().plusMonths(1));
                
                posting.setApplicationMethod("URL: " + url + "\n" + data.getOrDefault("applicationMethod", ""));
                posting.setExpectedSalary(data.getOrDefault("expectedSalary", "Negotiable"));
                posting.setStatus("PENDING");
                posting.setSource(source);

                JobPosting saved = jobPostingService.save(posting);
                if (firstSaved == null) firstSaved = saved;
            }

            updateSource(source, "SUCCESS");
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(firstSaved));

        } catch (Exception e) {
            log.error("Error during URL registration: {}", e.getMessage(), e);
            updateSource(source, "FAILED");
            saveExtractionLog(source, "gpt-4o", "ERROR", startTime, e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Failed to process URL: " + e.getMessage()));
        }
    }

    @PutMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobPosting>> updateJob(@PathVariable Long id, @RequestBody JobPosting jobPosting) {
        return jobPostingService.update(id, jobPosting)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
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
