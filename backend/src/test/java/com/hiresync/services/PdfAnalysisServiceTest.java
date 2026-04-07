package com.hiresync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.function.Consumer;

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
        // Mock ChatClient structure
        when(chatClientBuilder.build()).thenReturn(chatClient);
        
        pdfAnalysisService = spy(new PdfAnalysisService(chatClientBuilder));
    }

    @Test
    void shouldAnalyzePdfSuccessfully() {
        // Given
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        String query = "Extract details";
        String expectedResult = "{\"jobPostings\": [{\"companyName\": \"Test Corp\"}]}";

        // Mock PDF extraction to return something useful
        doReturn("This is a job posting for Java Developer at Test Corp.").when(pdfAnalysisService).extractTextFromPdf(any());

        // Mock ChatClient call chain
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResult);

        // When
        String result = pdfAnalysisService.analyzePdf(pdfResource, query);

        // Then
        assertEquals(expectedResult, result);
        verify(chatClient).prompt();
    }

    @Test
    void shouldReturnEmptyJsonWhenTextIsEmpty() {
        // Given
        Resource emptyResource = new ByteArrayResource(new byte[0]);
        doReturn("").when(pdfAnalysisService).extractTextFromPdf(any());
        
        // When
        String result = pdfAnalysisService.analyzePdf(emptyResource, "Extract details");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
        // Should not call AI
        verify(chatClient, never()).prompt();
    }
}
