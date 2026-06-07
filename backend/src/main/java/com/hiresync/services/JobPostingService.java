package com.hiresync.services;

import com.hiresync.models.JobPosting;
import com.hiresync.models.JobPostingRequest;
import com.hiresync.repositories.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;

    public Page<JobPosting> findAll(Pageable pageable) {
        return jobPostingRepository.findAll(pageable);
    }

    public Page<JobPosting> findByStatus(String status, Pageable pageable) {
        return jobPostingRepository.findByStatus(status, pageable);
    }

    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }

    public JobPosting save(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    public void deleteById(Long id) {
        jobPostingRepository.deleteById(id);
    }

    public Optional<JobPosting> update(Long id, JobPostingRequest request) {
        return jobPostingRepository.findById(id).map(jobPosting -> {
            if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
                jobPosting.setCompanyName(request.getCompanyName());
            }
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                jobPosting.setLocation(request.getLocation());
            }
            if (request.getPosition() != null && !request.getPosition().isBlank()) {
                jobPosting.setPosition(request.getPosition());
            }
            if (request.getRequirements() != null && !request.getRequirements().isBlank()) {
                jobPosting.setRequirements(request.getRequirements());
            }
            if (request.getApplicationMethod() != null && !request.getApplicationMethod().isBlank()) {
                jobPosting.setApplicationMethod(request.getApplicationMethod());
            }
            if (request.getExpectedSalary() != null && !request.getExpectedSalary().isBlank()) {
                jobPosting.setExpectedSalary(request.getExpectedSalary());
            }
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                jobPosting.setStatus(request.getStatus());
            }

            if (request.getOpeningDate() != null && !request.getOpeningDate().isBlank()) {
                try {
                    jobPosting.setOpeningDate(LocalDate.parse(request.getOpeningDate()));
                } catch (Exception e) {
                    // Invalid date format logging or handling can be added here
                }
            }
            if (request.getClosingDate() != null && !request.getClosingDate().isBlank()) {
                try {
                    jobPosting.setClosingDate(LocalDate.parse(request.getClosingDate()));
                } catch (Exception e) {
                    // Invalid date format logging or handling can be added here
                }
            }
            
            return jobPostingRepository.save(jobPosting);
        });
    }

    @org.springframework.transaction.annotation.Transactional
    public Optional<JobPosting> updateStatus(Long id, String status) {
        return jobPostingRepository.findById(id).map(jobPosting -> {
            jobPosting.setStatus(status);
            return jobPostingRepository.save(jobPosting);
        });
    }
}
