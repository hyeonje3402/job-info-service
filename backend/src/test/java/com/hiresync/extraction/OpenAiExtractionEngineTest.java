package com.hiresync.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenAiExtractionEngineTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    private ObjectMapper objectMapper;
    private OpenAiExtractionEngine extractionEngine;
    private Resource extractionPrompt;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        extractionPrompt = new ByteArrayResource("test prompt".getBytes(StandardCharsets.UTF_8));
        extractionEngine = new OpenAiExtractionEngine(objectMapper, httpClient);
        ReflectionTestUtils.setField(extractionEngine, "extractionPrompt", extractionPrompt);
        ReflectionTestUtils.setField(extractionEngine, "apiKey", "test-key");
        ReflectionTestUtils.setField(extractionEngine, "baseUrl", "http://localhost");
        ReflectionTestUtils.setField(extractionEngine, "modelName", "test-model");
    }

    @Test
    void shouldExtractJobPostingsSuccessfully() throws Exception {
        String rawText = "This is a job posting for Java Developer at Test Corp.";
        String aiResponseJson = "{\"jobPostings\":[{\"position\":\"Java Developer\",\"companyName\":\"Test Corp\"}]}";
        givenAiResponse(aiResponseJson);

        List<Map<String, String>> result = extractionEngine.extractJobPostings(rawText);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).get("position"));
        assertEquals("Test Corp", result.get(0).get("companyName"));

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest request = requestCaptor.getValue();
        assertEquals(URI.create("http://localhost/v1/chat/completions"), request.uri());
        assertEquals("Bearer test-key", request.headers().firstValue("Authorization").orElseThrow());
    }

    @Test
    void shouldReturnEmptyListWhenJsonIsInvalid() throws Exception {
        givenAiResponse("invalid json");

        List<Map<String, String>> result = extractionEngine.extractJobPostings("raw text");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenJobPostingsFieldIsMissing() throws Exception {
        givenAiResponse("{\"other\":[]}");

        List<Map<String, String>> result = extractionEngine.extractJobPostings("raw text");

        assertTrue(result.isEmpty());
    }

    private void givenAiResponse(String content) throws Exception {
        String responseBody = objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", content)))));
        doReturn(200).when(response).statusCode();
        doReturn(responseBody).when(response).body();
        doReturn(response).when(httpClient).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }
}
