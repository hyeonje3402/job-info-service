package com.hiresync.controllers;

import com.hiresync.models.JobPosting;
import com.hiresync.services.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class JobPostingController {
    private final JobPostingService jobPostingService;

    @GetMapping("/")
    public String index() {
        return "HireSync Backend API is running!";
    }

    @GetMapping("/api/v1/job-postings")
    public ResponseEntity<Page<JobPosting>> getAllJobPostings(Pageable pageable) {
        return ResponseEntity.ok(jobPostingService.findAll(pageable));
    }

    @GetMapping("/api/v1/job-postings/{id}")
    public ResponseEntity<JobPosting> getJobPostingById(@PathVariable Long id) {
        return jobPostingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
