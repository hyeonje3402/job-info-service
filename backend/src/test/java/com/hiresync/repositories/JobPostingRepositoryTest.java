package com.hiresync.repositories;

import com.hiresync.models.JobPosting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobPostingRepositoryTest {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Test
    void shouldFindByStatus() {
        // Given
        JobPosting job1 = createJobPosting("Company A", "Position A", "APPROVED");
        JobPosting job2 = createJobPosting("Company B", "Position B", "PENDING");
        jobPostingRepository.save(job1);
        jobPostingRepository.save(job2);

        // When
        List<JobPosting> approvedJobs = jobPostingRepository.findByStatus("APPROVED");

        // Then
        assertThat(approvedJobs).hasSize(1);
        assertThat(approvedJobs.get(0).getCompanyName()).isEqualTo("Company A");
    }

    @Test
    void shouldFindByPositionContainingIgnoreCase() {
        // Given
        JobPosting job1 = createJobPosting("Company A", "Software Engineer", "APPROVED");
        JobPosting job2 = createJobPosting("Company B", "Data Scientist", "APPROVED");
        jobPostingRepository.save(job1);
        jobPostingRepository.save(job2);

        // When
        List<JobPosting> found = jobPostingRepository.findByPositionContainingIgnoreCase("software");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPosition()).isEqualTo("Software Engineer");
    }

    private JobPosting createJobPosting(String company, String position, String status) {
        JobPosting job = new JobPosting();
        job.setCompanyName(company);
        job.setPosition(position);
        job.setStatus(status);
        job.setLocation("Remote");
        job.setRequirements("Requirements here");
        job.setOpeningDate(LocalDate.now());
        job.setClosingDate(LocalDate.now().plusDays(30));
        job.setApplicationMethod("Email");
        job.setExpectedSalary("Competitive");
        return job;
    }
}
