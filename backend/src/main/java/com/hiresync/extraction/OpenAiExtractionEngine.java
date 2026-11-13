package com.hiresync.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Component
@Slf4j
public class OpenAiExtractionEngine {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OpenAiExtractionEngine(ObjectMapper objectMapper) {
        this(objectMapper, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build());
    }

    OpenAiExtractionEngine(ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Value("classpath:prompts/job-extraction.st")
    private Resource extractionPrompt;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    public List<Map<String, String>> extractJobPostings(String rawText) {
        try {
            // 1. 프롬프트 템플릿 생성
            String templateText;
            try {
                templateText = StreamUtils.copyToString(extractionPrompt.getInputStream(), StandardCharsets.UTF_8);
            } catch (java.io.IOException ioe) {
                throw new RuntimeException("프롬프트 템플릿 로드 실패", ioe);
            }
            String promptText = templateText.replace("{text}", rawText);

            // 2. Request Body JSON 직렬화
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("temperature", 0);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", promptText);
            
            requestBody.put("messages", List.of(message));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            log.info("Sending raw HTTP request via java.net.http.HttpClient to: {}", baseUrl);

            // 3. 순수 java.net.http.HttpClient를 사용하여 curl과 동일한 최소 헤더 및 HTTP/1.1로 호출
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.error("LLM Server returned non-200 status: {}. Body: {}", response.statusCode(), response.body());
                return Collections.emptyList();
            }

            String responseBody = response.body();
            if (responseBody == null || responseBody.isBlank()) {
                log.error("LLM Server returned empty body.");
                return Collections.emptyList();
            }

            Map<String, Object> apiResponse = objectMapper.readValue(responseBody, Map.class);

            if (apiResponse == null || !apiResponse.containsKey("choices")) {
                log.error("LLM Server returned null or empty response.");
                return Collections.emptyList();
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("LLM Server returned empty choices.");
                return Collections.emptyList();
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> responseMessage = (Map<String, Object>) choice.get("message");
            if (responseMessage == null || !responseMessage.containsKey("content")) {
                log.error("LLM Server choice message or content is null.");
                return Collections.emptyList();
            }

            String content = (String) responseMessage.get("content");
            log.debug("LLM Raw Response Content: {}", content);

            // 마크다운 JSON 블럭 포맷 정제
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                if (content.contains("```")) {
                    content = content.substring(0, content.lastIndexOf("```"));
                }
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3);
                if (content.contains("```")) {
                    content = content.substring(0, content.lastIndexOf("```"));
                }
            }
            content = content.trim();

            try {
                JsonNode root = objectMapper.readTree(content);
                JsonNode postingsNode = root.get("jobPostings");
                if (postingsNode != null && postingsNode.isArray()) {
                    List<Map<String, String>> result = new ArrayList<>();
                    for (JsonNode node : postingsNode) {
                        Map<String, String> map = objectMapper.convertValue(node, Map.class);
                        result.add(map);
                    }
                    return result;
                } else if (root.isArray()) {
                    List<Map<String, String>> result = new ArrayList<>();
                    for (JsonNode node : root) {
                        Map<String, String> map = objectMapper.convertValue(node, Map.class);
                        result.add(map);
                    }
                    return result;
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse LLM response JSON: {}", content, e);
            }

        } catch (Exception e) {
            log.error("Exception during LLM extraction: {}", e.getMessage(), e);
            throw new RuntimeException("LLM 정보 추출 중 오류 발생: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
