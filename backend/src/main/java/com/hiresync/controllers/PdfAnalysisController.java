package com.hiresync.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.models.JobPosting;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/jobs")
public class PdfAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisController.class);
    private final PdfAnalysisService pdfAnalysisService;
    private final JobPostingService jobPostingService;
    private final ObjectMapper objectMapper;

    public PdfAnalysisController(PdfAnalysisService pdfAnalysisService, JobPostingService jobPostingService, ObjectMapper objectMapper) {
        this.pdfAnalysisService = pdfAnalysisService;
        this.jobPostingService = jobPostingService;
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
            result = pdfAnalysisService.analyzePdf(pdfResource, "Extract job details: company name, location, category/field, conditions, period/deadline, method, salary.");
            log.debug("AI Raw Analysis Result: {}", result);

            // 2. JSON 파싱
            JobPosting extractedData;
            try {
                extractedData = objectMapper.readValue(result, JobPosting.class);
            } catch (Exception e) {
                log.error("Failed to parse AI response to JobPosting. Raw result: {}", result, e);
                return ResponseEntity.status(500).body(Map.of(
                    "error", "JSON Parsing Error: " + e.getMessage(),
                    "rawResult", result
                ));
            }
            
            // 3. 필수 정보 보완 및 기본값 설정 (필수 필드인 companyName, field가 null이면 DB 저장 실패 방지)
            if (extractedData.getCompanyName() == null || extractedData.getCompanyName().isEmpty()) {
                extractedData.setCompanyName("Unknown Company (Extracted)");
            }
            if (extractedData.getField() == null || extractedData.getField().isEmpty()) {
                extractedData.setField("Unknown Field (Extracted)");
            }
            
            extractedData.setSource(JobPosting.SourceType.PDF);
            extractedData.setSourceOrigin(file.getOriginalFilename());
            
            // 4. DB에 저장
            log.info("Saving extracted job posting to DB");
            JobPosting saved = jobPostingService.save(extractedData);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("status", "SUCCESS");
            response.put("extractedData", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("IOException reading file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error reading PDF file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during PDF analysis", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Unexpected Error: " + e.getMessage(),
                "rawResult", result
            ));
        }
    }
}
