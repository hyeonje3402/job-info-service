package com.hiresync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
import com.hiresync.services.PdfAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class PdfAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfAnalysisService pdfAnalysisService;

    @MockBean
    private JobPostingService jobPostingService;

    @MockBean
    private SourceRepository sourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile mockPdf;

    @BeforeEach
    void setUp() {
        mockPdf = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf content".getBytes()
        );

        Source mockSource = new Source();
        mockSource.setId(1L);
        when(sourceRepository.save(any(Source.class))).thenReturn(mockSource);
    }

    @Test
    void shouldAnalyzePdfSuccessfully() throws Exception {
        String jsonResult = "{\"jobPostings\": [{" +
                "\"companyName\": \"Test Corp\", " +
                "\"location\": \"Seoul\", " +
                "\"position\": \"Java Developer\", " +
                "\"requirements\": \"Java 21\", " +
                "\"openingDate\": \"2024-01-01\", " +
                "\"closingDate\": \"2024-12-31\", " +
                "\"applicationMethod\": \"Email\", " +
                "\"expectedSalary\": \"6000\"" +
                "}]}";

        when(pdfAnalysisService.analyzePdf(any(), any())).thenReturn(jsonResult);

        JobPosting savedJob = new JobPosting();
        savedJob.setId(100L);
        savedJob.setCompanyName("Test Corp");
        savedJob.setPosition("Java Developer");
        when(jobPostingService.save(any(JobPosting.class))).thenReturn(savedJob);

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Test Corp"))
                .andExpect(jsonPath("$.data.position").value("Java Developer"));
    }

    @Test
    void shouldHandleMarkdownJsonResult() throws Exception {
        String markdownResult = "Here is the data: \n```json\n" +
                "{\"companyName\": \"Markdown Corp\", \"position\": \"Frontend Developer\"}" +
                "\n```";

        when(pdfAnalysisService.analyzePdf(any(), any())).thenReturn(markdownResult);

        JobPosting savedJob = new JobPosting();
        savedJob.setCompanyName("Markdown Corp");
        savedJob.setPosition("Frontend Developer");
        when(jobPostingService.save(any(JobPosting.class))).thenReturn(savedJob);

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value("Markdown Corp"));
    }

    @Test
    void shouldHandleEmptyExtraction() throws Exception {
        when(pdfAnalysisService.analyzePdf(any(), any())).thenReturn("{}");

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.data").value("No data extracted"));
    }

    @Test
    void shouldHandleUnexpectedError() throws Exception {
        when(pdfAnalysisService.analyzePdf(any(), any())).thenThrow(new RuntimeException("AI Service Down"));

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.data").value("Unexpected error during PDF analysis"));
    }
}
