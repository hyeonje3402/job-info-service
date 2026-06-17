package com.hiresync.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class OpenAiExtractionEngineTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OpenAiExtractionEngine extractionEngine;

    private Resource extractionPrompt;

    @BeforeEach
    void setUp() {
        extractionPrompt = new ByteArrayResource("test prompt".getBytes());
        ReflectionTestUtils.setField(extractionEngine, "extractionPrompt", extractionPrompt);
        
        when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    void shouldExtractJobPostingsSuccessfully() throws JsonProcessingException {
        // Given
        String rawText = "This is a job posting for Java Developer at Test Corp.";
        String aiResponseJson = "{\"jobPostings\": [{\"title\": \"Java Developer\", \"company\": \"Test Corp\"}]}";

        when(chatClient.prompt(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse);
        
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage(aiResponseJson));

        // Mock token usage
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);
        when(chatResponse.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getPromptTokens()).thenReturn(10);
        when(usage.getCompletionTokens()).thenReturn(20);
        when(usage.getTotalTokens()).thenReturn(30);

        JsonNode rootNode = mock(JsonNode.class);
        JsonNode postingsNode = mock(JsonNode.class);
        when(objectMapper.readTree(aiResponseJson)).thenReturn(rootNode);
        when(rootNode.get("jobPostings")).thenReturn(postingsNode);
        when(postingsNode.isArray()).thenReturn(true);
        when(postingsNode.iterator()).thenReturn(List.of(mock(JsonNode.class)).iterator());
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(Map.of("title", "Java Developer", "company", "Test Corp"));

        // When
        List<Map<String, String>> result = extractionEngine.extractJobPostings(rawText);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).get("title"));
        verify(chatClient).prompt(any(org.springframework.ai.chat.prompt.Prompt.class));
    }

    @Test
    void shouldReturnEmptyListWhenJsonIsInvalid() throws JsonProcessingException {
        // Given
        String aiResponseJson = "invalid json";
        when(chatClient.prompt(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage(aiResponseJson));

        when(objectMapper.readTree(aiResponseJson)).thenThrow(JsonProcessingException.class);

        // When
        List<Map<String, String>> result = extractionEngine.extractJobPostings("raw text");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenJobPostingsFieldIsMissing() throws JsonProcessingException {
        // Given
        String aiResponseJson = "{\"other\": []}";
        when(chatClient.prompt(any(org.springframework.ai.chat.prompt.Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage(aiResponseJson));

        JsonNode rootNode = mock(JsonNode.class);
        when(objectMapper.readTree(aiResponseJson)).thenReturn(rootNode);
        when(rootNode.get("jobPostings")).thenReturn(null);

        // When
        List<Map<String, String>> result = extractionEngine.extractJobPostings("raw text");

        // Then
        assertTrue(result.isEmpty());
    }
}
