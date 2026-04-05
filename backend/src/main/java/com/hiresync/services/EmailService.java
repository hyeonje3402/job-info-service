package com.hiresync.services;

import com.hiresync.extraction.OpenAiExtractionEngine;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.SourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final OpenAiExtractionEngine extractionEngine;
    private final JobPostingService jobPostingService;
    private final SourceRepository sourceRepository;

    private static final String EMAIL_SUBJECT_FILTER = "[연암취업정보]";
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");

    /**
     * 수신된 이메일 처리
     */
    public void processIncomingEmail(String subject, String body, String senderEmail) {
        if (!subject.contains(EMAIL_SUBJECT_FILTER)) {
            log.info("Email subject does not match filter. Skipping. Subject: {}", subject);
            return;
        }

        log.info("Processing job info email from: {}", senderEmail);

        // 1. Source 저장
        Source source = new Source();
        source.setType("email");
        source.setStatus("PENDING");
        source = sourceRepository.save(source);

        try {
            // 2. 본문에서 URL 추출 시도
            Matcher matcher = URL_PATTERN.matcher(body);
            StringBuilder combinedContext = new StringBuilder(body);
            
            // 3. AI 엔진을 통한 정보 추출
            List<Map<String, String>> extractedData = extractionEngine.extractJobPostings(combinedContext.toString());

            for (Map<String, String> data : extractedData) {
                JobPosting posting = new JobPosting();
                posting.setCompanyName(data.getOrDefault("companyName", "Unknown"));
                posting.setLocation(data.getOrDefault("location", "Unknown"));
                posting.setPosition(data.getOrDefault("position", "Unknown"));
                posting.setRequirements(data.getOrDefault("requirements", "N/A"));
                
                // 날짜 처리 (기본값 설정)
                posting.setOpeningDate(LocalDate.now());
                posting.setClosingDate(LocalDate.now().plusMonths(1));
                
                posting.setApplicationMethod("Email from: " + senderEmail + "\n" + data.getOrDefault("applicationMethod", ""));
                posting.setExpectedSalary(data.getOrDefault("expectedSalary", "Negotiable"));
                posting.setStatus("PENDING");
                posting.setSource(source);

                jobPostingService.save(posting);
            }

            source.setStatus("SUCCESS");
            sourceRepository.save(source);
            log.info("Successfully processed email and saved {} job postings", extractedData.size());

        } catch (Exception e) {
            log.error("Failed to process email from {}: {}", senderEmail, e.getMessage());
            source.setStatus("FAILED");
            source.setErrorMessage(e.getMessage());
            sourceRepository.save(source);
        }
    }
}
