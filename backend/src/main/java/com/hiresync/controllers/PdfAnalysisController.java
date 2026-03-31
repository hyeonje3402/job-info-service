package com.hiresync.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.ExtractionLog;
import com.hiresync.models.JobPosting;
import com.hiresync.repositories.ExtractionLogRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/jobs")
public class PdfAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisController.class);
    private final PdfAnalysisService pdfAnalysisService;
    private final JobPostingService jobPostingService;
    private final ExtractionLogRepository extractionLogRepository;
    private final ObjectMapper objectMapper;

    public PdfAnalysisController(PdfAnalysisService pdfAnalysisService, JobPostingService jobPostingService, ExtractionLogRepository extractionLogRepository, ObjectMapper objectMapper) {
        this.pdfAnalysisService = pdfAnalysisService;
        this.jobPostingService = jobPostingService;
        this.extractionLogRepository = extractionLogRepository;
        // 알 수 없는 속성이 있어도 파싱 실패하지 않도록 설정
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzePdf(@RequestParam("pdfFile") MultipartFile file) {
        String result = "";
        try {
            log.info("Starting PDF analysis for file: {}", file.getOriginalFilename());
            Resource pdfResource = new ByteArrayResource(file.getBytes());
            
            // 1. AI 분석 수행
            result = pdfAnalysisService.analyzePdf(pdfResource, "Extract all job details: company name, location, category/field, conditions, period/deadline, method, salary.");
            log.debug("AI Raw Analysis Result: {}", result);

            // 마크다운 코드 블록 제거
            if (result.contains("```json")) {
                result = result.substring(result.indexOf("```json") + 7);
                result = result.substring(0, result.lastIndexOf("```"));
            } else if (result.contains("```")) {
                result = result.substring(result.indexOf("```") + 3);
                result = result.substring(0, result.lastIndexOf("```"));
            }
            result = result.trim();

            // 2. JSON 파싱 (유연한 처리)
            List<JobPosting> extractedPostings = new ArrayList<>();
            try {
                JsonNode rootNode = objectMapper.readTree(result);
                if (rootNode.isArray()) {
                    log.debug("Root node is an array");
                    extractedPostings = objectMapper.convertValue(rootNode, new TypeReference<List<JobPosting>>() {});
                } else if (rootNode.has("jobPostings")) {
                    JsonNode jobPostingsNode = rootNode.get("jobPostings");
                    log.debug("Found 'jobPostings' node. IsArray: {}", jobPostingsNode.isArray());
                    
                    if (jobPostingsNode.isArray()) {
                        extractedPostings = objectMapper.convertValue(jobPostingsNode, new TypeReference<List<JobPosting>>() {});
                    } else if (jobPostingsNode.isObject()) {
                        JobPosting single = objectMapper.convertValue(jobPostingsNode, JobPosting.class);
                        if (single != null && single.getCompanyName() != null) {
                            extractedPostings.add(single);
                        }
                    }
                } else if (rootNode.isObject()) {
                    log.debug("Root node is an object");
                    // 단일 객체인 경우
                    JobPosting single = objectMapper.convertValue(rootNode, JobPosting.class);
                    if (single != null && single.getCompanyName() != null) {
                        extractedPostings.add(single);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse AI response. Raw result: {}", result, e);
                saveLog(file.getOriginalFilename(), "FAIL", "JSON Parsing Error: " + e.getMessage(), result);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "JSON Parsing Error: " + e.getMessage(),
                    "rawResult", result
                ));
            }
            
            if (extractedPostings.isEmpty()) {
                log.warn("No job postings extracted from AI response");
                saveLog(file.getOriginalFilename(), "SUCCESS_EMPTY", "No data extracted", result);
            }

            List<JobPosting> savedPostings = new ArrayList<>();
            for (JobPosting extractedData : extractedPostings) {
                // 3. 필수 정보 보완 및 기본값 설정
                if (extractedData.getCompanyName() == null || extractedData.getCompanyName().trim().isEmpty()) {
                    extractedData.setCompanyName("Unknown Company");
                }
                if (extractedData.getField() == null || extractedData.getField().trim().isEmpty()) {
                    extractedData.setField("General");
                }
                
                extractedData.setSource(JobPosting.SourceType.PDF);
                extractedData.setSourceOrigin(file.getOriginalFilename());
                
                // 4. DB에 저장
                log.info("Saving extracted job posting ({}) to DB", extractedData.getCompanyName());
                savedPostings.add(jobPostingService.save(extractedData));
            }

            // 성공 로그 기록
            saveLog(file.getOriginalFilename(), "SUCCESS", "Extracted and saved " + savedPostings.size() + " jobs", result);

            Map<String, Object> response = new HashMap<>();
            response.put("count", savedPostings.size());
            response.put("status", "SUCCESS");
            response.put("extractedData", savedPostings);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("IOException reading file: {}", file.getOriginalFilename(), e);
            saveLog(file.getOriginalFilename(), "FAIL", "IOException: " + e.getMessage(), null);
            return ResponseEntity.status(500).body(Map.of("error", "Error reading PDF file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during PDF analysis", e);
            saveLog(file.getOriginalFilename(), "FAIL", "Unexpected Error: " + e.getMessage(), result);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Unexpected Error: " + e.getMessage(),
                "rawResult", result
            ));
        }
    }

    private void saveLog(String filename, String status, String message, String data) {
        try {
            ExtractionLog exLog = new ExtractionLog();
            exLog.setSourceType("PDF");
            exLog.setSourceOrigin(filename);
            exLog.setStatus(status);
            exLog.setErrorMessage(message);
            exLog.setExtractedData(data);
            extractionLogRepository.save(exLog);
        } catch (Exception e) {
            log.error("Failed to save extraction log", e);
        }
    }
}
