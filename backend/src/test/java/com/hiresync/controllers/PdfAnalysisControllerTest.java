package com.hiresync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
import com.hiresync.services.PdfAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

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

    @MockBean
    private com.hiresync.repositories.ExtractionLogRepository extractionLogRepository;

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
    @DisplayName("analyzePdf: jobPostings 키가 포함된 JSON 응답 처리")
    void shouldAnalyzePdfWithJobPostingsKey() throws Exception {
        String jsonResult = "{\"jobPostings\": [{\"companyName\": \"Test Corp\", \"position\": \"Java Developer\"}]}";
        performSuccessfulMock(jsonResult, "Test Corp");
    }

    @Test
    @DisplayName("analyzePdf: 루트가 배열인 JSON 응답 처리")
    void shouldAnalyzePdfWithRootArray() throws Exception {
        String jsonResult = "[{\"companyName\": \"Array Corp\", \"position\": \"Developer\"}]";
        performSuccessfulMock(jsonResult, "Array Corp");
    }

    @Test
    @DisplayName("analyzePdf: postings 키가 포함된 JSON 응답 처리")
    void shouldAnalyzePdfWithPostingsKey() throws Exception {
        String jsonResult = "{\"postings\": [{\"companyName\": \"Postings Corp\", \"position\": \"Developer\"}]}";
        performSuccessfulMock(jsonResult, "Postings Corp");
    }

    @Test
    @DisplayName("analyzePdf: jobs 키가 포함된 JSON 응답 처리")
    void shouldAnalyzePdfWithJobsKey() throws Exception {
        String jsonResult = "{\"jobs\": [{\"companyName\": \"Jobs Corp\", \"position\": \"Developer\"}]}";
        performSuccessfulMock(jsonResult, "Jobs Corp");
    }

    @Test
    @DisplayName("analyzePdf: data 키가 포함된 JSON 응답 처리")
    void shouldAnalyzePdfWithDataKey() throws Exception {
        String jsonResult = "{\"data\": [{\"companyName\": \"Data Corp\", \"position\": \"Developer\"}]}";
        performSuccessfulMock(jsonResult, "Data Corp");
    }

    @Test
    @DisplayName("analyzePdf: data 키의 값이 단일 객체인 경우 처리")
    void shouldAnalyzePdfWithSingleDataObject() throws Exception {
        String jsonResult = "{\"data\": {\"companyName\": \"Single Data Corp\", \"position\": \"Developer\"}}";
        performSuccessfulMock(jsonResult, "Single Data Corp");
    }

    @Test
    @DisplayName("analyzePdf: 루트 자체가 단일 공고 객체인 경우 처리")
    void shouldAnalyzePdfWithRootObject() throws Exception {
        String jsonResult = "{\"companyName\": \"Root Object Corp\", \"position\": \"Developer\"}";
        performSuccessfulMock(jsonResult, "Root Object Corp");
    }

    @Test
    @DisplayName("analyzePdf: 마크다운 코드 블록이 포함된 응답 처리")
    void shouldHandleMarkdownJsonResult() throws Exception {
        String markdownResult = "Result: \n```json\n" +
                "{\"companyName\": \"Markdown Corp\", \"position\": \"Frontend Developer\"}" +
                "\n```";

        performSuccessfulMock(markdownResult, "Markdown Corp");
    }

    @Test
    @DisplayName("analyzePdf: PDF 텍스트 추출 결과가 비어있는 경우")
    void shouldHandleBlankPdfContent() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("");

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Empty PDF content"));
    }

    @Test
    @DisplayName("analyzePdf: AI 분석 결과가 null인 경우")
    void shouldHandleNullAiResult() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        when(pdfAnalysisService.analyzeText(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("AI extraction failed"));
    }

    @Test
    @DisplayName("analyzePdf: 잘못된 JSON 형식 응답 처리")
    void shouldHandleInvalidJsonResponse() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        when(pdfAnalysisService.analyzeText(any())).thenReturn("Invalid JSON string");

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to parse AI response"));
    }

    @Test
    @DisplayName("analyzePdf: 파일 읽기 중 IOException 발생")
    void shouldHandleIOException() throws Exception {
        MockMultipartFile errorFile = new MockMultipartFile("file", "error.pdf", "application/pdf", new byte[0]) {
            @Override
            public byte[] getBytes() throws java.io.IOException {
                throw new java.io.IOException("File read error");
            }
        };

        mockMvc.perform(multipart("/api/v1/pdf").file(errorFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Error reading PDF file"));
    }

    @Test
    @DisplayName("analyzePdf: 데이터베이스 저장 실패 (모든 저장이 예외 발생)")
    void shouldHandleDatabaseSaveFailure() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        when(pdfAnalysisService.analyzeText(any())).thenReturn("{\"companyName\": \"Save Fail Corp\"}");
        when(jobPostingService.save(any(JobPosting.class))).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Database save failure"));
    }

    @Test
    @DisplayName("analyzePdf: json 키워드 없는 마크다운 코드 블록 처리")
    void shouldHandlePlainMarkdownBlock() throws Exception {
        String plainBlock = "Result is: \n```\n" +
                "{\"companyName\": \"Plain Block Corp\", \"position\": \"Backend Developer\"}" +
                "\n```";

        performSuccessfulMock(plainBlock, "Plain Block Corp");
    }

    @Test
    @DisplayName("analyzePdf: 필드 누락 시 모든 기본값 적용 확인")
    void shouldApplyAllDefaultValues() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        // 거의 모든 필드가 누락된 JSON
        when(pdfAnalysisService.analyzeText(any())).thenReturn("{\"position\": \"Minimalist\"}");
        
        JobPosting savedJob = new JobPosting();
        savedJob.setCompanyName("Unknown Company");
        savedJob.setPosition("Minimalist");
        savedJob.setLocation("Unknown");
        savedJob.setRequirements("N/A");
        savedJob.setExpectedSalary("Negotiable");
        when(jobPostingService.save(any(JobPosting.class))).thenReturn(savedJob);

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyName").value("Unknown Company"))
                .andExpect(jsonPath("$.data.location").value("Unknown"))
                .andExpect(jsonPath("$.data.requirements").value("N/A"))
                .andExpect(jsonPath("$.data.expectedSalary").value("Negotiable"));
    }

    private void performSuccessfulMock(String result, String expectedCompanyName) throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        when(pdfAnalysisService.analyzeText(any())).thenReturn(result);

        JobPosting savedJob = new JobPosting();
        savedJob.setCompanyName(expectedCompanyName);
        savedJob.setPosition("Developer");
        when(jobPostingService.save(any(JobPosting.class))).thenReturn(savedJob);

        mockMvc.perform(multipart("/api/v1/pdf").file(mockPdf))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.companyName").value(expectedCompanyName));
    }

    @Test
    void shouldHandleEmptyExtraction() throws Exception {
        when(pdfAnalysisService.extractTextFromPdf(any())).thenReturn("dummy raw text");
        when(pdfAnalysisService.analyzeText(any())).thenReturn("{}");

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("No data extracted"));
    }

    @Test
    void shouldHandleUnexpectedError() throws Exception {
        when(pdfAnalysisService.analyzePdf(any(), any())).thenThrow(new RuntimeException("AI Service Down"));

        mockMvc.perform(multipart("/api/v1/pdf")
                .file(mockPdf))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Unexpected error during PDF analysis"));
    }
}
