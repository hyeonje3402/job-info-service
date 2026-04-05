package com.hiresync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

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
        
        pdfAnalysisService = new PdfAnalysisService(chatClientBuilder);
        
        // Mock Resource for extraction prompt
        Resource mockPrompt = new ByteArrayResource("Extract info from: {context}".getBytes());
        ReflectionTestUtils.setField(pdfAnalysisService, "extractionPrompt", mockPrompt);
    }

    @Test
    void shouldAnalyzePdfSuccessfully() {
        // Given
        // Tika can read text from plain bytes if it's not a complex PDF
        Resource pdfResource = new ByteArrayResource("This is a job posting for Java Developer at Test Corp.".getBytes());
        String query = "Extract details";
        String expectedResult = "{\"jobPostings\": [{\"companyName\": \"Test Corp\"}]}";

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
        verify(requestSpec).user(any(Consumer.class));
        verify(callResponseSpec).content();
    }

    @Test
    void shouldReturnEmptyJsonWhenTextIsEmpty() {
        // Given
        // Using an empty resource that Tika won't find text in
        Resource emptyResource = new ByteArrayResource(new byte[0]);
        
        // When
        String result = pdfAnalysisService.analyzePdf(emptyResource, "Extract details");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
        // Should not call AI
        verify(chatClient, never()).prompt();
    }
}
