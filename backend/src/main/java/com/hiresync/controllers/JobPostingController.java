package com.hiresync.controllers;

import com.hiresync.models.JobPosting;
import com.hiresync.services.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostingController {
    private final JobPostingService jobPostingService;

    @GetMapping("/")
    public String index() {
        return "HireSync Backend API is running!";
    }

    @GetMapping("/job-postings")
    public ResponseEntity<Page<JobPosting>> getAllJobPostings(Pageable pageable) {
        return ResponseEntity.ok(jobPostingService.findAll(pageable));
    }

    @GetMapping("/job-postings/{id}")
    public ResponseEntity<JobPosting> getJobPostingById(@PathVariable Long id) {
        return jobPostingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/job-postings/register-url")
    public ResponseEntity<?> registerUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        // Placeholder for URL extraction logic
        JobPosting jobPosting = new JobPosting();
        jobPosting.setSource(JobPosting.SourceType.URL);
        jobPosting.setSourceOrigin(url);
        jobPosting.setCompanyName("Extracted from URL"); // Mock data
        jobPosting.setField("Backend Developer");
        JobPosting saved = jobPostingService.save(jobPosting);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", saved.getId(),
            "status", "SUCCESS"
        ));
    }

    @PutMapping("/admin/job-postings/{id}")
    public ResponseEntity<?> updateJobPosting(@PathVariable Long id, @RequestBody JobPosting jobPosting) {
        return jobPostingService.update(id, jobPosting)
                .map(updated -> ResponseEntity.ok(Map.of("message", "Updated successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/job-postings/{id}")
    public ResponseEntity<Void> deleteJobPosting(@PathVariable Long id) {
        jobPostingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
