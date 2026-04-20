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
            if (updatedJobPosting.getCompanyName() != null) jobPosting.setCompanyName(updatedJobPosting.getCompanyName());
            if (updatedJobPosting.getLocation() != null) jobPosting.setLocation(updatedJobPosting.getLocation());
            if (updatedJobPosting.getPosition() != null) jobPosting.setPosition(updatedJobPosting.getPosition());
            if (updatedJobPosting.getRequirements() != null) jobPosting.setRequirements(updatedJobPosting.getRequirements());
            if (updatedJobPosting.getOpeningDate() != null) jobPosting.setOpeningDate(updatedJobPosting.getOpeningDate());
            if (updatedJobPosting.getClosingDate() != null) jobPosting.setClosingDate(updatedJobPosting.getClosingDate());
            if (updatedJobPosting.getApplicationMethod() != null) jobPosting.setApplicationMethod(updatedJobPosting.getApplicationMethod());
            if (updatedJobPosting.getExpectedSalary() != null) jobPosting.setExpectedSalary(updatedJobPosting.getExpectedSalary());
            if (updatedJobPosting.getStatus() != null) jobPosting.setStatus(updatedJobPosting.getStatus());
            if (updatedJobPosting.getSource() != null) jobPosting.setSource(updatedJobPosting.getSource());
            return jobPostingRepository.save(jobPosting);
        });
    }
}
