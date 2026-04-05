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
import java.util.Map;

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
        String result = "";
        Source source = saveSource("pdf", "PENDING", null, null);
        
        try {
            log.info("Starting PDF analysis for file: {}", file.getOriginalFilename());
            Resource pdfResource = new ByteArrayResource(file.getBytes());
            
            result = pdfAnalysisService.analyzePdf(pdfResource, "Extract all job details: company name, location, category/position, requirements, openingDate, closingDate, application method, expected salary.");

            if (result.contains("```json")) {
                result = result.substring(result.indexOf("```json") + 7);
                result = result.substring(0, result.lastIndexOf("```"));
            } else if (result.contains("```")) {
                result = result.substring(result.indexOf("```") + 3);
                result = result.substring(0, result.lastIndexOf("```"));
            }
            result = result.trim();

            List<JobPosting> extractedPostings = new ArrayList<>();
            JsonNode rootNode = objectMapper.readTree(result);
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
                updateSource(source, "FAILED", "No data extracted", result);
                return ResponseEntity.ok(new ApiResponse<>("error", "No data extracted"));
            }

            JobPosting firstSaved = null;
            for (JobPosting extractedData : extractedPostings) {
                if (extractedData.getCompanyName() == null) extractedData.setCompanyName("Unknown Company");
                if (extractedData.getLocation() == null) extractedData.setLocation("Unknown");
                if (extractedData.getPosition() == null) extractedData.setPosition("General");
                if (extractedData.getRequirements() == null) extractedData.setRequirements("N/A");
                if (extractedData.getOpeningDate() == null) extractedData.setOpeningDate(LocalDate.now());
                if (extractedData.getClosingDate() == null) extractedData.setClosingDate(LocalDate.now().plusMonths(1));
                if (extractedData.getApplicationMethod() == null) extractedData.setApplicationMethod("N/A");
                if (extractedData.getExpectedSalary() == null) extractedData.setExpectedSalary("Negotiable");
                
                extractedData.setStatus("PENDING");
                extractedData.setSource(source);
                
                JobPosting saved = jobPostingService.save(extractedData);
                if (firstSaved == null) firstSaved = saved;
            }

            updateSource(source, "SUCCESS", null, result);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(firstSaved));
            
        } catch (IOException e) {
            updateSource(source, "FAILED", "IOException: " + e.getMessage(), null);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Error reading PDF file"));
        } catch (Exception e) {
            updateSource(source, "FAILED", "Unexpected Error: " + e.getMessage(), result);
            return ResponseEntity.status(500).body(new ApiResponse<>("error", "Unexpected error during PDF analysis"));
        }
    }

    private Source saveSource(String type, String status, String message, String data) {
        Source s = new Source();
        s.setType(type);
        s.setStatus(status);
        s.setErrorMessage(message);
        s.setExtractedData(data);
        return sourceRepository.save(s);
    }

    private void updateSource(Source s, String status, String message, String data) {
        s.setStatus(status);
        s.setErrorMessage(message);
        s.setExtractedData(data);
        sourceRepository.save(s);
    }
}
