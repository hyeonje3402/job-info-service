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

    public JobPosting save(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    public void deleteById(Long id) {
        jobPostingRepository.deleteById(id);
    }

    public Optional<JobPosting> update(Long id, JobPosting updatedJobPosting) {
        return jobPostingRepository.findById(id).map(jobPosting -> {
            jobPosting.setCompanyName(updatedJobPosting.getCompanyName());
            jobPosting.setLocation(updatedJobPosting.getLocation());
            jobPosting.setPosition(updatedJobPosting.getPosition());
            jobPosting.setRequirements(updatedJobPosting.getRequirements());
            jobPosting.setOpeningDate(updatedJobPosting.getOpeningDate());
            jobPosting.setClosingDate(updatedJobPosting.getClosingDate());
            jobPosting.setApplicationMethod(updatedJobPosting.getApplicationMethod());
            jobPosting.setExpectedSalary(updatedJobPosting.getExpectedSalary());
            jobPosting.setStatus(updatedJobPosting.getStatus());
            jobPosting.setSource(updatedJobPosting.getSource());
            return jobPostingRepository.save(jobPosting);
        });
    }
}
