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
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PdfAnalysisServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock(answer = org.mockito.Answers.RETURNS_SELF)
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private PdfAnalysisService pdfAnalysisService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        
        pdfAnalysisService = spy(new PdfAnalysisService(chatClientBuilder, restClientBuilder));
    }

    @Test
    void shouldAnalyzePdfSuccessfully() throws Exception {
        // Given
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        String expectedResult = "{\"jobPostings\": [{\"companyName\": \"Test Corp\"}]}";

        doReturn("Extracted text").when(pdfAnalysisService).extractTextFromPdf(any());
        
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{\"choices\":[{\"message\":{\"content\":\"{\\\"jobPostings\\\": [{\\\"companyName\\\": \\\"Test Corp\\\"}]}\"}}]}");

        // When
        String result = pdfAnalysisService.analyzePdf(pdfResource, "query");

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnEmptyJsonWhenTextIsEmpty() throws Exception {
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
    void shouldHandleAiNullResponse() throws Exception {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{\"choices\":[{\"message\":{\"content\":null}}]}");

        // When
        String result = pdfAnalysisService.analyzeText("some context");

        // Then
        assertEquals("{\"jobPostings\": []}", result);
    }

    @Test
    void shouldHandleAiException() throws Exception {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("AI Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pdfAnalysisService.analyzeText("some context");
        });
        
        assertTrue(exception.getMessage().contains("LLM Gateway 호출 중 오류가 발생했습니다: AI Error"));
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
    void shouldHandleAiEmptyResponse() throws Exception {
        // Given
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{\"choices\":[{\"message\":{\"content\":\"   \"}}]}");

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

