package com.hiresync.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlScrapingServiceTest {

    private UrlScrapingService urlScrapingService;
    private MockedStatic<Jsoup> mockedJsoup;

    @BeforeEach
    void setUp() {
        urlScrapingService = new UrlScrapingService();
        mockedJsoup = mockStatic(Jsoup.class);
    }

    @AfterEach
    void tearDown() {
        mockedJsoup.close();
    }

    @Test
    void shouldScrapeUrlSuccessfully() throws IOException {
        // Given
        String url = "https://example.com/job";
        String html = "<html><body><h1>Job Posting</h1><p>Company: Hiring Co</p></body></html>";
        String cleanedHtml = "Job Posting Company: Hiring Co";
        
        Connection connection = mock(Connection.class);
        Document document = mock(Document.class);
        Element body = mock(Element.class);
        Document parsedDoc = mock(Document.class);
        
        when(Jsoup.connect(url)).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.followRedirects(anyBoolean())).thenReturn(connection);
        when(connection.referrer(anyString())).thenReturn(connection);
        when(connection.get()).thenReturn(document);
        when(document.body()).thenReturn(body);
        when(body.html()).thenReturn(html);
        
        mockedJsoup.when(() -> Jsoup.clean(anyString(), any())).thenReturn(cleanedHtml);
        mockedJsoup.when(() -> Jsoup.parse(anyString())).thenReturn(parsedDoc);
        when(parsedDoc.text()).thenReturn("Cleaned Text Result");

        // When
        String result = urlScrapingService.scrapeUrl(url);

        // Then
        assertEquals("Cleaned Text Result", result);
        verify(Jsoup.class);
        Jsoup.connect(url);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenScrapingFails() throws IOException {
        // Given
        String url = "https://invalid-url.com";
        Connection connection = mock(Connection.class);
        
        when(Jsoup.connect(url)).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.followRedirects(anyBoolean())).thenReturn(connection);
        when(connection.referrer(anyString())).thenReturn(connection);
        when(connection.get()).thenThrow(new IOException("Connection refused"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            urlScrapingService.scrapeUrl(url);
        });

        assertTrue(exception.getMessage().contains("URL 연결 실패"));
    }

    @Test
    void shouldHandleEmptyBodySuccessfully() throws IOException {
        // Given
        String url = "https://example.com/empty";
        Connection connection = mock(Connection.class);
        Document document = mock(Document.class);
        Element body = mock(Element.class);
        Document parsedDoc = mock(Document.class);
        
        when(Jsoup.connect(url)).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.followRedirects(anyBoolean())).thenReturn(connection);
        when(connection.referrer(anyString())).thenReturn(connection);
        when(connection.get()).thenReturn(document);
        when(document.body()).thenReturn(body);
        when(body.html()).thenReturn("");

        mockedJsoup.when(() -> Jsoup.clean(anyString(), any())).thenReturn("");
        mockedJsoup.when(() -> Jsoup.parse(anyString())).thenReturn(parsedDoc);
        when(parsedDoc.text()).thenReturn("");

        // When
        String result = urlScrapingService.scrapeUrl(url);

        // Then
        assertEquals("", result);
    }
}
