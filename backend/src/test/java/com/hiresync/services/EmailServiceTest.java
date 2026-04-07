package com.hiresync.services;

import com.hiresync.extraction.OpenAiExtractionEngine;
import com.hiresync.models.JobPosting;
import com.hiresync.models.Source;
import com.hiresync.repositories.ExtractionLogRepository;
import com.hiresync.repositories.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private OpenAiExtractionEngine extractionEngine;

    @Mock
    private JobPostingService jobPostingService;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private ExtractionLogRepository extractionLogRepository;

    @InjectMocks
    private EmailService emailService;

    private String subject;
    private String body;
    private String senderEmail;

    @BeforeEach
    void setUp() {
        subject = "[연암취업정보] 2024년 신입 개발자 채용";
        body = "테스트 이메일 본문입니다.";
        senderEmail = "hr@example.com";
    }

    @Test
    void shouldSkipEmailWithoutFilter() {
        // Given
        String wrongSubject = "일반 메일 제목";

        // When
        emailService.processIncomingEmail(wrongSubject, body, senderEmail);

        // Then
        verify(sourceRepository, never()).save(any(Source.class));
        verify(extractionEngine, never()).extractJobPostings(anyString());
    }

    @Test
    void shouldProcessEmailSuccessfully() {
        // Given
        Source mockSource = new Source();
        mockSource.setId(1L);
        when(sourceRepository.save(any(Source.class))).thenReturn(mockSource);

        List<Map<String, String>> mockExtractedData = new ArrayList<>();
        Map<String, String> job1 = new HashMap<>();
        job1.put("companyName", "Test Corp");
        job1.put("position", "Java Developer");
        mockExtractedData.add(job1);

        when(extractionEngine.extractJobPostings(anyString())).thenReturn(mockExtractedData);

        // When
        emailService.processIncomingEmail(subject, body, senderEmail);

        // Then
        verify(sourceRepository, atLeastOnce()).save(any(Source.class));
        verify(extractionEngine).extractJobPostings(anyString());
        verify(jobPostingService).save(any(JobPosting.class));
        
        // Final status should be SUCCESS
        verify(sourceRepository).save(argThat(s -> "SUCCESS".equals(s.getStatus())));
    }

    @Test
    void shouldHandleExtractionError() {
        // Given
        Source mockSource = new Source();
        when(sourceRepository.save(any(Source.class))).thenReturn(mockSource);
        
        when(extractionEngine.extractJobPostings(anyString())).thenThrow(new RuntimeException("AI Engine Error"));

        // When
        emailService.processIncomingEmail(subject, body, senderEmail);

        // Then
        // Should update status to FAILED
        verify(sourceRepository).save(argThat(s -> "FAILED".equals(s.getStatus())));
    }
}
