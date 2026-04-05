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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        post.setLocation("Seoul");
        post.setPosition("Backend Developer");
        post.setRequirements("Java, Spring");
        post.setOpeningDate(LocalDate.now());
        post.setClosingDate(LocalDate.now().plusMonths(1));
        post.setApplicationMethod("Email");
        post.setExpectedSalary("5000");
        post.setStatus("APPROVED");
        jobPostingRepository.save(post);
    }

    @Test
    void shouldReturnJobPostingsList() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].companyName").value("Test Company"));
    }

    @Test
    void shouldReturnJobPostingDetail() throws Exception {
        JobPosting savedPost = jobPostingRepository.findAll().get(0);
        
        mockMvc.perform(get("/api/v1/jobs/" + savedPost.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Test Company"))
                .andExpect(jsonPath("$.data.position").value("Backend Developer"));
    }

    @Test
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(post("/api/v1/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
