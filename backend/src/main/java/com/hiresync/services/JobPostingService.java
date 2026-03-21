package com.hiresync.services;

import com.hiresync.models.JobPosting;
import com.hiresync.repositories.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;

    public Page<JobPosting> findAll(Pageable pageable) {
        return jobPostingRepository.findAll(pageable);
    }

    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }
}
