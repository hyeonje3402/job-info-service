package com.hiresync.controllers;

import com.hiresync.models.JobPosting;
import com.hiresync.repositories.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    private Long targetId;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();

        JobPosting post = new JobPosting();
        post.setCompanyName("Admin Test Company");
        post.setLocation("Seoul");
        post.setPosition("Developer");
        post.setRequirements("Requirements");
        post.setOpeningDate(LocalDate.now());
        post.setClosingDate(LocalDate.now().plusMonths(1));
        post.setApplicationMethod("Email");
        post.setExpectedSalary("4000");
        post.setStatus("PENDING");

        JobPosting saved = jobPostingRepository.save(post);
        targetId = saved.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateJobPostingWhenAdmin() throws Exception {
        String updatedJson = "{\"companyName\":\"Updated Company\",\"status\":\"APPROVED\",\"location\":\"Seoul\",\"position\":\"Developer\",\"requirements\":\"Requirements\",\"openingDate\":\"2026-04-03\",\"closingDate\":\"2026-05-03\",\"applicationMethod\":\"Email\",\"expectedSalary\":\"4000\"}";

        mockMvc.perform(put("/api/v1/jobs/" + targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Updated Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteJobPostingWhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/jobs/" + targetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldFailWhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/jobs/" + targetId))
                .andExpect(status().isForbidden());
    }
}
