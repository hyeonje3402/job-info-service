package com.hiresync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.extraction.OpenAiExtractionEngine;
import com.hiresync.models.JobPosting;
import com.hiresync.repositories.JobPostingRepository;
import com.hiresync.services.UrlScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlScrapingService urlScrapingService;

    @MockBean
    private OpenAiExtractionEngine extractionEngine;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
    }

    private JobPosting createMockJobPosting(String companyName, String position) {
        JobPosting post = new JobPosting();
        post.setCompanyName(companyName);
        post.setLocation("Seoul");
        post.setPosition(position);
        post.setRequirements("Java, Spring");
        post.setOpeningDate(LocalDate.now());
        post.setClosingDate(LocalDate.now().plusMonths(1));
        post.setApplicationMethod("Email");
        post.setExpectedSalary("5000");
        post.setStatus("APPROVED");
        return post;
    }

    @Test
    void getAllJobs_WhenJobsExist_ReturnsPageOfJobs() throws Exception {
        // Given
        jobPostingRepository.save(createMockJobPosting("Company A", "Backend"));
        jobPostingRepository.save(createMockJobPosting("Company B", "Frontend"));

        // When
        mockMvc.perform(get("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].companyName").value("Company A"))
                .andExpect(jsonPath("$.data.content[1].companyName").value("Company B"));
    }

    @Test
    void getAllJobs_WhenNoJobsExist_ReturnsEmptyPage() throws Exception {
        // Given (Empty repository due to setUp and @Transactional)

        // When
        mockMvc.perform(get("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
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
    void shouldReturn404WhenJobNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(post("/api/v1/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateJobPosting() throws Exception {
        JobPosting savedPost = jobPostingRepository.findAll().get(0);
        savedPost.setCompanyName("Updated Company");
        
        mockMvc.perform(put("/api/v1/jobs/" + savedPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Updated Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUpdatingNonExistentJob() throws Exception {
        JobPosting job = new JobPosting();
        job.setCompanyName("Ghost Company");
        
        mockMvc.perform(put("/api/v1/jobs/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteJobPosting() throws Exception {
        JobPosting savedPost = jobPostingRepository.findAll().get(0);
        
        mockMvc.perform(delete("/api/v1/jobs/" + savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        
        assert jobPostingRepository.findById(savedPost.getId()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRegisterUrlSuccessfully() throws Exception {
        String testUrl = "https://example.com/job";
        String rawText = "Job at Example Co";
        Map<String, String> extractedData = new HashMap<>();
        extractedData.put("companyName", "Example Co");
        extractedData.put("position", "Software Engineer");

        Mockito.when(urlScrapingService.scrapeUrl(testUrl)).thenReturn(rawText);
        Mockito.when(extractionEngine.extractJobPostings(rawText)).thenReturn(List.of(extractedData));

        Map<String, String> request = Map.of("url", testUrl);
        
        mockMvc.perform(post("/api/v1/url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Example Co"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenAiExtractionEmpty() throws Exception {
        String testUrl = "https://example.com/job";
        Mockito.when(urlScrapingService.scrapeUrl(testUrl)).thenReturn("Garbage text");
        Mockito.when(extractionEngine.extractJobPostings(anyString())).thenReturn(Collections.emptyList());

        Map<String, String> request = Map.of("url", testUrl);
        
        mockMvc.perform(post("/api/v1/url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("No data extracted from URL"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenUrlScrapingFails() throws Exception {
        String testUrl = "https://invalid-url.com";
        Mockito.when(urlScrapingService.scrapeUrl(testUrl)).thenThrow(new RuntimeException("Connection failed"));

        Map<String, String> request = Map.of("url", testUrl);
        
        mockMvc.perform(post("/api/v1/url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
