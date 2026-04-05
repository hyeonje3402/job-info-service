package com.hiresync.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.ApiResponse;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.SourceRepository;
import com.hiresync.services.JobPostingService;
import com.hiresync.services.PdfAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PdfAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisController.class);
    private final PdfAnalysisService pdfAnalysisService;
    private final JobPostingService jobPostingService;
    private final SourceRepository sourceRepository;
    private final ObjectMapper objectMapper;

    public PdfAnalysisController(PdfAnalysisService pdfAnalysisService, JobPostingService jobPostingService, SourceRepository sourceRepository, ObjectMapper objectMapper) {
        this.pdfAnalysisService = pdfAnalysisService;
        this.jobPostingService = jobPostingService;
        this.sourceRepository = sourceRepository;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> analyzePdf(@RequestParam("file") MultipartFile file) {
        String aiResult = "";
        Source source = saveSource("pdf", "PENDING", null, null, null);
        
        try {
            log.info("Starting PDF analysis for file: {}", file.getOriginalFilename());
            Resource pdfResource = new ByteArrayResource(file.getBytes());
            
            // 1. Extract raw text from PDF
            String rawText = pdfAnalysisService.extractTextFromPdf(pdfResource);
            source.setRawText(rawText);
            sourceRepository.save(source); // Save raw text early

            if (rawText.isBlank()) {
                updateSource(source, "FAILED", "Could not extract text from PDF", null, null);
                return ResponseEntity.ok(new ApiResponse<>("error", "Empty PDF content"));
            }

            // 2. Analyze text with AI
            aiResult = pdfAnalysisService.analyzeText(rawText);
            log.debug("AI Raw Response: {}", aiResult);

            // Clean up JSON response if markdown is present
            if (aiResult.contains("```json")) {
                aiResult = aiResult.substring(aiResult.indexOf("```json") + 7);
                aiResult = aiResult.substring(0, aiResult.lastIndexOf("```"));
            } else if (aiResult.contains("```")) {
                aiResult = aiResult.substring(aiResult.indexOf("```") + 3);
                aiResult = aiResult.substring(0, aiResult.lastIndexOf("```"));
            }
            aiResult = aiResult.trim();

            List<JobPosting> extractedPostings = new ArrayList<>();
            JsonNode rootNode = objectMapper.readTree(aiResult);
            if (rootNode.isArray()) {
                extractedPostings = objectMapper.convertValue(rootNode, new TypeReference<List<JobPosting>>() {});
            } else if (rootNode.has("jobPostings")) {
                JsonNode jobPostingsNode = rootNode.get("jobPostings");
                if (jobPostingsNode.isArray()) {
                    extractedPostings = objectMapper.convertValue(jobPostingsNode, new TypeReference<List<JobPosting>>() {});
                } else if (jobPostingsNode.isObject()) {
                    JobPosting single = objectMapper.convertValue(jobPostingsNode, JobPosting.class);
                    if (single != null && single.getCompanyName() != null) {
                        extractedPostings.add(single);
                    }
                }
            } else if (rootNode.isObject()) {
                JobPosting single = objectMapper.convertValue(rootNode, JobPosting.class);
                if (single != null && single.getCompanyName() != null) {
                    extractedPostings.add(single);
                }
            }
            
            if (extractedPostings.isEmpty()) {
                log.warn("No data extracted from AI response. Raw result: {}", aiResult);
                updateSource(source, "FAILED", "No data extracted from AI", aiResult, rawText);
                return ResponseEntity.ok(new ApiResponse<>("error", "No data extracted"));
            }

            JobPosting firstSaved = null;
            int savedCount = 0;
            for (JobPosting extractedData : extractedPostings) {
                // Ensure all non-nullable fields are set
                if (extractedData.getCompanyName() == null || extractedData.getCompanyName().isBlank()) extractedData.setCompanyName("Unknown Company");
                if (extractedData.getLocation() == null || extractedData.getLocation().isBlank()) extractedData.setLocation("Unknown");
                if (extractedData.getPosition() == null || extractedData.getPosition().isBlank()) extractedData.setPosition("General");
                if (extractedData.getRequirements() == null || extractedData.getRequirements().isBlank()) extractedData.setRequirements("N/A");
                if (extractedData.getOpeningDate() == null) extractedData.setOpeningDate(LocalDate.now());
                if (extractedData.getClosingDate() == null) extractedData.setClosingDate(LocalDate.now().plusMonths(1));
                if (extractedData.getApplicationMethod() == null || extractedData.getApplicationMethod().isBlank()) extractedData.setApplicationMethod("N/A");
                if (extractedData.getExpectedSalary() == null || extractedData.getExpectedSalary().isBlank()) extractedData.setExpectedSalary("Negotiable");
                
                extractedData.setStatus("PENDING");
                extractedData.setSource(source);
                
                try {
                    JobPosting saved = jobPostingService.save(extractedData);
                    savedCount++;
                    if (firstSaved == null) firstSaved = saved;
                } catch (Exception e) {
                    log.error("Failed to save JobPosting: {}. Error: {}", extractedData.getCompanyName(), e.getMessage());
                }
            }

            if (savedCount > 0) {
                log.info("Successfully saved {} job postings", savedCount);
                updateSource(source, "SUCCESS", null, aiResult, rawText);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(firstSaved));
            } else {
                updateSource(source, "FAILED", "Failed to save any data to database", aiResult, rawText);
                return ResponseEntity.status(500).body(new ApiResponse<>("error", "Database save failure"));
            }
            
        } catch (IOException e) {
            log.error("IOException during PDF analysis: {}", e.getMessage());
            updateSource(source, "FAILED", "IOException: " + e.getMessage(), null, null);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Error reading PDF file"));
        } catch (Exception e) {
            log.error("Unexpected error during PDF analysis: {}", e.getMessage(), e);
            updateSource(source, "FAILED", "Unexpected Error: " + e.getMessage(), aiResult, null);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Unexpected error during PDF analysis"));
        }
    }

    private Source saveSource(String type, String status, String message, String data, String rawText) {
        Source s = new Source();
        s.setType(type);
        s.setStatus(status);
        s.setErrorMessage(message);
        s.setExtractedData(data);
        s.setRawText(rawText);
        return sourceRepository.save(s);
    }

    private void updateSource(Source s, String status, String message, String data, String rawText) {
        s.setStatus(status);
        s.setErrorMessage(message);
        s.setExtractedData(data);
        if (rawText != null) s.setRawText(rawText);
        sourceRepository.save(s);
    }
}
