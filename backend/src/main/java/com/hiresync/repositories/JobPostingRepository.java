package com.hiresync.repositories;

import com.hiresync.models.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatus(String status);
    Page<JobPosting> findByStatus(String status, Pageable pageable);
    List<JobPosting> findByPositionContainingIgnoreCase(String position);
}
