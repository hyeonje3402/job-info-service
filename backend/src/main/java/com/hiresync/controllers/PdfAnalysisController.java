package com.hiresync.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.ApiResponse;
import com.hiresync.models.ExtractionLog;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.ExtractionLogRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PdfAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisController.class);
    private final PdfAnalysisService pdfAnalysisService;
    private final JobPostingService jobPostingService;
    private final SourceRepository sourceRepository;
    private final ExtractionLogRepository extractionLogRepository;
    private final ObjectMapper objectMapper;

    public PdfAnalysisController(PdfAnalysisService pdfAnalysisService, JobPostingService jobPostingService, 
                                 SourceRepository sourceRepository, ExtractionLogRepository extractionLogRepository,
                                 ObjectMapper objectMapper) {
        this.pdfAnalysisService = pdfAnalysisService;
        this.jobPostingService = jobPostingService;
        this.sourceRepository = sourceRepository;
        this.extractionLogRepository = extractionLogRepository;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> analyzePdf(@RequestParam("file") MultipartFile file) {
        String aiResult = "";
        Source source = null;
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting PDF analysis for file: {}", file.getOriginalFilename());
            
            // 1. Source 저장
            source = new Source();
            source.setType("PDF");
            source.setReferenceUri(file.getOriginalFilename());
            source.setStatus("PROCESSING");
            source = sourceRepository.save(source);
            
            Resource pdfResource = new ByteArrayResource(file.getBytes());
            
            // 2. Extract raw text from PDF
            String rawText = pdfAnalysisService.extractTextFromPdf(pdfResource);

            if (rawText.isBlank()) {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", "EMPTY_PDF", startTime, "Empty PDF content");
                return ResponseEntity.ok(new ApiResponse<>("error", "Empty PDF content"));
            }

            // 3. Analyze text with AI
            aiResult = pdfAnalysisService.analyzeText(rawText);

            if (aiResult == null || aiResult.isBlank()) {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", "AI_FAILURE", startTime, "AI extraction failed");
                return ResponseEntity.ok(new ApiResponse<>("error", "AI extraction failed"));
            }

            // Clean up JSON response
            String jsonOnly = cleanJsonResponse(aiResult);

            List<JobPosting> extractedPostings = new ArrayList<>();
            try {
                JsonNode rootNode = objectMapper.readTree(jsonOnly);
                extractedPostings = parseJobPostings(rootNode);
            } catch (Exception e) {
                log.error("Failed to parse AI JSON response. Error: {}. Cleaned JSON: {}", e.getMessage(), jsonOnly);
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", aiResult, startTime, "JSON Parsing Error: " + e.getMessage());
                return ResponseEntity.ok(new ApiResponse<>("error", "Failed to parse AI response"));
            }
            
            if (extractedPostings.isEmpty()) {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", aiResult, startTime, "No job postings found in AI response");
                return ResponseEntity.ok(new ApiResponse<>("error", "No data extracted"));
            }

            JobPosting firstSaved = saveExtractedPostings(extractedPostings, source);

            if (firstSaved != null) {
                updateSource(source, "SUCCESS");
                saveExtractionLog(source, "gpt-4o", aiResult, startTime, null);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(firstSaved));
            } else {
                updateSource(source, "FAILED");
                saveExtractionLog(source, "gpt-4o", aiResult, startTime, "Database save failure");
                return ResponseEntity.status(500).body(new ApiResponse<>("error", "Database save failure"));
            }
            
        } catch (IOException e) {
            log.error("IOException during PDF analysis: {}", e.getMessage());
            if (source != null) updateSource(source, "FAILED");
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Error reading PDF file"));
        } catch (Exception e) {
            log.error("Unexpected error during PDF analysis: {}", e.getMessage(), e);
            if (source != null) updateSource(source, "FAILED");
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Unexpected error during PDF analysis"));
        }
    }

    private String cleanJsonResponse(String aiResult) {
        String jsonOnly = aiResult.trim();
        if (jsonOnly.contains("```json")) {
            jsonOnly = jsonOnly.substring(jsonOnly.indexOf("```json") + 7);
            if (jsonOnly.contains("```")) {
                jsonOnly = jsonOnly.substring(0, jsonOnly.lastIndexOf("```"));
            }
        } else if (jsonOnly.contains("```")) {
            jsonOnly = jsonOnly.substring(jsonOnly.indexOf("```") + 3);
            if (jsonOnly.contains("```")) {
                jsonOnly = jsonOnly.substring(0, jsonOnly.lastIndexOf("```"));
            }
        }
        return jsonOnly.trim();
    }

    private List<JobPosting> parseJobPostings(JsonNode rootNode) {
        List<JobPosting> extractedPostings = new ArrayList<>();
        if (rootNode.isArray()) {
            extractedPostings = objectMapper.convertValue(rootNode, new TypeReference<List<JobPosting>>() {});
        } else {
            JsonNode dataNode = null;
            if (rootNode.has("jobPostings")) dataNode = rootNode.get("jobPostings");
            else if (rootNode.has("postings")) dataNode = rootNode.get("postings");
            else if (rootNode.has("jobs")) dataNode = rootNode.get("jobs");
            else if (rootNode.has("data")) dataNode = rootNode.get("data");

            if (dataNode != null && dataNode.isArray()) {
                extractedPostings = objectMapper.convertValue(dataNode, new TypeReference<List<JobPosting>>() {});
            } else if (dataNode != null && dataNode.isObject() && !dataNode.isEmpty()) {
                JobPosting single = objectMapper.convertValue(dataNode, JobPosting.class);
                if (single != null) extractedPostings.add(single);
            } else if (rootNode.isObject() && !rootNode.isEmpty()) {
                if (rootNode.has("companyName") || rootNode.has("position")) {
                    JobPosting single = objectMapper.convertValue(rootNode, JobPosting.class);
                    if (single != null) extractedPostings.add(single);
                }
            }
        }
        return extractedPostings;
    }

    private JobPosting saveExtractedPostings(List<JobPosting> postings, Source source) {
        JobPosting firstSaved = null;
        for (JobPosting data : postings) {
            if (data.getCompanyName() == null) data.setCompanyName("Unknown Company");
            if (data.getLocation() == null) data.setLocation("Unknown");
            if (data.getPosition() == null) data.setPosition("General");
            if (data.getRequirements() == null) data.setRequirements("N/A");
            if (data.getOpeningDate() == null) data.setOpeningDate(LocalDate.now());
            if (data.getClosingDate() == null) data.setClosingDate(LocalDate.now().plusMonths(1));
            if (data.getApplicationMethod() == null) data.setApplicationMethod("N/A");
            if (data.getExpectedSalary() == null) data.setExpectedSalary("Negotiable");
            
            data.setStatus("PENDING");
            data.setSource(source);
            
            try {
                JobPosting saved = jobPostingService.save(data);
                if (firstSaved == null) firstSaved = saved;
            } catch (Exception e) {
                log.error("Failed to save JobPosting: {}", e.getMessage());
            }
        }
        return firstSaved;
    }

    private void updateSource(Source s, String status) {
        s.setStatus(status);
        sourceRepository.save(s);
    }

    private void saveExtractionLog(Source source, String model, String data, long startTime, String error) {
        ExtractionLog logEntry = new ExtractionLog();
        logEntry.setSource(source);
        logEntry.setModelVersion(model);
        logEntry.setRawData(data);
        logEntry.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));
        logEntry.setErrorMessage(error);
        logEntry.setProcessedAt(LocalDateTime.now());
        extractionLogRepository.save(logEntry);
    }
}
