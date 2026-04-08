package com.hiresync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfAnalysisServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private PdfAnalysisService pdfAnalysisService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        pdfAnalysisService = spy(new PdfAnalysisService(chatClientBuilder));
    }

    @Test
    void shouldAnalyzePdfSuccessfully() {
        // Given
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        String expectedResult = "{\"jobPostings\": [{\"companyName\": \"Test Corp\"}]}";

        doReturn("Extracted text").when(pdfAnalysisService).extractTextFromPdf(any());
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResult);

        // When
        String result = pdfAnalysisService.analyzePdf(pdfResource, "query");

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnEmptyJsonWhenTextIsEmpty() {
        // Given
        Resource emptyResource = new ByteArrayResource(new byte[0]);
        doReturn("").when(pdfAnalysisService).extractTextFromPdf(any());

        // When
        String result = pdfAnalysisService.analyzePdf(emptyResource, "query");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
        verify(chatClient, never()).prompt(any(Prompt.class));
    }

    @Test
    void shouldHandleAiNullResponse() {
        // Given
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(null);

        // When
        String result = pdfAnalysisService.analyzeText("some context");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
    }

    @Test
    void shouldHandleAiException() {
        // Given
        when(chatClient.prompt(any(Prompt.class))).thenThrow(new RuntimeException("AI Error"));

        // When
        String result = pdfAnalysisService.analyzeText("some context");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
    }

    @Test
    void shouldReturnEmptyStringWhenPdfExtractionFails() throws IOException {
        // Given
        Resource faultyResource = mock(Resource.class);
        when(faultyResource.getInputStream()).thenThrow(new IOException("Read error"));

        // When
        String result = pdfAnalysisService.extractTextFromPdf(faultyResource);

        // Then
        assertEquals("", result);
    }

    @Test
    void shouldHandleBlankContextInAnalyzeText() {
        // When
        String result = pdfAnalysisService.analyzeText("   ");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
    }
}

