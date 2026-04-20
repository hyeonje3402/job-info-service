package com.hiresync.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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
    void shouldExtractTextFromValidPdf() throws IOException {
        // Given: Create a simple PDF in memory
        byte[] pdfBytes;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("  Hello   World  "); // Contains multiple spaces and leading/trailing spaces
                contentStream.endText();
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            document.save(baos);
            pdfBytes = baos.toByteArray();
        }

        Resource pdfResource = new ByteArrayResource(pdfBytes);

        // When
        String result = pdfAnalysisService.extractTextFromPdf(pdfResource);

        // Then: Should be cleaned (single space, trimmed)
        assertEquals("Hello World", result);
    }

    @Test
    void shouldHandleInvalidPdfFormat() {
        // Given: Invalid PDF bytes
        Resource invalidPdf = new ByteArrayResource("not a pdf".getBytes());

        // When
        String result = pdfAnalysisService.extractTextFromPdf(invalidPdf);

        // Then
        assertEquals("", result);
    }

    @Test
    void shouldHandleAiEmptyResponse() {
        // Given
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("   ");

        // When
        String result = pdfAnalysisService.analyzeText("some context");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
    }

    @Test
    void shouldReturnEmptyStringWhenInputIsNullInExtractText() {
        // When
        String result = pdfAnalysisService.extractTextFromPdf(null);

        // Then
        assertEquals("", result);
    }
}

