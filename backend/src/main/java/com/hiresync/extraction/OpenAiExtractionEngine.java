package com.hiresync.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiExtractionEngine {
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/job-extraction.st")
    private Resource extractionPrompt;

    public List<Map<String, String>> extractJobPostings(String rawText) {
        ChatClient chatClient = chatClientBuilder.build();
        
        // 1. PromptTemplate을 사용하여 안전하게 텍스트 주입
        PromptTemplate template = new PromptTemplate(extractionPrompt);
        Prompt prompt = template.create(Map.of("text", rawText));
        
        // 2. ChatClient를 통해 AI 호출
        ChatResponse chatResponse = chatClient.prompt(prompt)
            .call()
            .chatResponse();

        if (chatResponse != null && chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
            log.info("AI Token Usage - Prompt: {}, Completion: {}, Total: {}", 
                chatResponse.getMetadata().getUsage().getPromptTokens(),
                chatResponse.getMetadata().getUsage().getCompletionTokens(),
                chatResponse.getMetadata().getUsage().getTotalTokens());
        }

        String response = chatResponse.getResult().getOutput().getText();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode postingsNode = root.get("jobPostings");
            if (postingsNode != null && postingsNode.isArray()) {
                List<Map<String, String>> result = new ArrayList<>();
                for (JsonNode node : postingsNode) {
                    Map<String, String> map = objectMapper.convertValue(node, Map.class);
                    result.add(map);
                }
                return result;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", response, e);
        }

        return Collections.emptyList();
    }
}
