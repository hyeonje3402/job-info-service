package com.hiresync.services;

import com.hiresync.models.JobPosting;
import com.hiresync.repositories.JobPostingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    @Test
    void shouldReturnAllJobPostingsPaged() {
        // Given
        JobPosting post1 = new JobPosting();
        post1.setId(1L);
        post1.setCompanyName("Company A");

        JobPosting post2 = new JobPosting();
        post2.setId(2L);
        post2.setCompanyName("Company B");

        List<JobPosting> list = Arrays.asList(post1, post2);
        Page<JobPosting> page = new PageImpl<>(list);

        when(jobPostingRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        Page<JobPosting> result = jobPostingService.findAll(PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Company A");
    }

    @Test
    void shouldReturnJobPostingById() {
        // Given
        JobPosting post = new JobPosting();
        post.setId(1L);
        post.setCompanyName("Company A");
        
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(post));

        // When
        Optional<JobPosting> result = jobPostingService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("Company A");
    }
}
