package com.hiresync.controllers;

import com.hiresync.models.JobPosting;
import com.hiresync.repositories.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
        
        JobPosting post = new JobPosting();
        post.setCompanyName("Test Company");
        post.setJobCategory("Backend Developer");
        post.setSourceType("URL");
        jobPostingRepository.save(post);
    }

    @Test
    void shouldReturnJobPostingsList() throws Exception {
        mockMvc.perform(get("/api/v1/job-postings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].companyName").value("Test Company"));
    }

    @Test
    void shouldReturnJobPostingDetail() throws Exception {
        JobPosting savedPost = jobPostingRepository.findAll().get(0);
        
        mockMvc.perform(get("/api/v1/job-postings/" + savedPost.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.jobCategory").value("Backend Developer"));
    }
}
