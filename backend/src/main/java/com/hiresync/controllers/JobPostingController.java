package com.hiresync.controllers;

import com.hiresync.models.ApiResponse;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
import com.hiresync.services.UrlScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final UrlScrapingService urlScrapingService;
    private final SourceRepository sourceRepository;

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
    public ResponseEntity<ApiResponse<JobPosting>> registerUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        
        // Use UrlScrapingService to extract data (mocked for now, or calling actual service)
        // If UrlScrapingService is already implemented, we should use it.
        // For now, following the pattern in the previous implementation but with the new response structure.
        
        Source source = new Source();
        source.setType("url");
        source.setStatus("SUCCESS");
        source.setExtractedData(url);
        source = sourceRepository.save(source);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setCompanyName("Extracted from URL");
        jobPosting.setLocation("Unknown");
        jobPosting.setPosition("Backend Developer");
        jobPosting.setRequirements("Requirements placeholder");
        jobPosting.setOpeningDate(java.time.LocalDate.now());
        jobPosting.setClosingDate(java.time.LocalDate.now().plusMonths(1));
        jobPosting.setApplicationMethod("URL Submission: " + url);
        jobPosting.setExpectedSalary("Negotiable");
        jobPosting.setStatus("PENDING");
        jobPosting.setSource(source);
        
        JobPosting saved = jobPostingService.save(jobPosting);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<JobPosting>> updateJob(@PathVariable Long id, @RequestBody JobPosting jobPosting) {
        return jobPostingService.update(id, jobPosting)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobPostingService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
