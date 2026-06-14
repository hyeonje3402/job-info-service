package com.hiresync.services;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.Map;

@Service
public class PdfAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisService.class);
    private final ChatClient chatClient;

    private final String promptText = """
        당신은 텍스트를 구조화된 JSON 데이터로 완벽하게 변환하는 데이터 엔지니어 에이전트입니다.
        아래 [텍스트 내용]에 나열된 모든 채용 공고 항목들을 추출하여 하나의 JSON 객체로 만드십시오.
        
        [엄격 지침]
        1. 텍스트 내의 모든 채용 공고를 하나도 빠짐없이 전부 추출하여 'jobPostings' 배열에 담으십시오.
        2. 날짜 필드(openingDate, closingDate)는 반드시 'YYYY-MM-DD' 형식으로 변환하여 기재해야 합니다. (연도가 없는 '03-25(수)'와 같은 형식은 올해 연도인 2026년을 붙여 '2026-03-25'로 변환하십시오)
        3. 공고 내에 시작일(openingDate) 정보가 제공되지 않았다면 임의로 작성하지 말고 반드시 null로 지정하십시오.
        4. 근무지가 여러 곳인 경우(예: '사천, 고성') 쉼표로 연결된 하나의 문자열로 작성하십시오.
        5. 다른 마크다운 백틱(```json ... ```)이나 서론, 결론 없이 오직 유효한 JSON 문자열만 출력해야 합니다.
        
        [JSON 구조 및 Few-Shot 예시]
        {json_structure}
        
        [텍스트 내용]
        {pdf_content}
        """;

    private final String jsonStructure = """
        {
          "jobPostings": [
            {
              "companyName": "SK키파운드리",
              "location": "청주",
              "position": "제조 오퍼레이터",
              "requirements": "고졸 이상, 교대 근무 및 방진복 착용 가능자",
              "openingDate": null,
              "closingDate": "2026-03-25",
              "applicationMethod": "https://www.skcareers.com/Recruit/Detail/R260585",
              "expectedSalary": "내규에 따름"
            },
            {
              "companyName": "에스앤케이항공㈜",
              "location": "사천, 고성",
              "position": "조립",
              "requirements": "학력 무관, 한국항공우주산업㈜ 파견 평가에 따라 근무 가능자",
              "openingDate": null,
              "closingDate": "2026-03-26",
              "applicationMethod": "https://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=53244508",
              "expectedSalary": "내규에 따름"
            }
          ]
        }
        """;

    public PdfAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String extractTextFromPdf(Resource pdfResource) {
        try (InputStream is = pdfResource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);
                return cleanText(text);
            }
        } catch (Exception e) {
            log.error("PDFBox extraction error: {}", e.getMessage(), e);
            return "";
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";
        // 제어 문자 제거
        String cleaned = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        
        // 줄바꿈 보존하며 공백 정제
        cleaned = cleaned.replaceAll("[ \\t\\r\\f]+", " ");
        cleaned = cleaned.replaceAll("\\n+", "\n");
        
        // 정교한 인라인 메타데이터 필터링 (한 줄 텍스트 대응 정규식 치환)
        cleaned = cleaned.replaceAll("ㆍ기업형태\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ업종\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ설립연도\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ사원수\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ매출액\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ홈페이지\\s*:[^ㆍ\\n]*", "");
        cleaned = cleaned.replaceAll("ㆍ주소\\s*:[^ㆍ\\n]*", "");
        
        return cleaned.trim();
    }

    public String analyzeText(String context) throws Exception {
        if (context == null || context.isBlank()) return "{\"jobPostings\": []}";
        
        log.info("Starting AI analysis... (Text length: {})", context.length());
        
        String systemInstructions = """
            당신은 텍스트에서 채용 공고 정보를 완벽하게 추출하여 구조화하는 데이터 엔지니어 에이전트입니다.
            제공된 텍스트에서 모든 개별 채용 모집 정보를 추출하여 아래의 JSON 구조에 맞게 JSON 객체로 반환하십시오.
            다른 서론, 결론, 부연설명이나 마크다운 백틱(```json) 없이 오직 유효한 JSON 포맷만 답변하십시오.
            
            [엄격 지침]
            1. 텍스트 내의 모든 채용 공고를 하나도 빠짐없이 전부 추출하여 'jobPostings' 배열에 담으십시오.
            2. 날짜 필드(openingDate, closingDate)는 반드시 'YYYY-MM-DD' 형식으로 변환하여 기재해야 합니다. (연도가 없는 '03-25(수)'와 같은 형식은 올해 연도인 2026년을 붙여 '2026-03-25'로 변환하십시오)
            3. 공고 내에 시작일(openingDate) 정보가 제공되지 않았다면 임의로 작성하지 말고 반드시 null로 지정하십시오.
            4. 근무지가 여러 곳인 경우(예: '사천, 고성') 쉼표로 연결된 하나의 문자열로 작성하십시오.
            
            [JSON 구조 및 Few-Shot 예시]
            """ + jsonStructure;

        String userText = "[분석할 텍스트]\n" + context;
        
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", "yc-gemma-4-26b-a4b");
        requestBody.put("temperature", 0);
        
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(java.util.Map.of("role", "system", "content", systemInstructions));
        messages.add(java.util.Map.of("role", "user", "content", userText));
        requestBody.put("messages", messages);
        
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "sk-n4b-HXuTkrclWDvt11zViA";
        }

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(120000);
            requestFactory.setReadTimeout(120000);

            RestClient customRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl("http://llm.ycbjkim.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept-Encoding", "identity")
                .build();
                
            String responseJson = customRestClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);
                
            log.info("Raw LLM Response: {}", responseJson);
            
            ObjectMapper tempMapper = new ObjectMapper();
            JsonNode root = tempMapper.readTree(responseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            return content;
        } catch (Exception e) {
            log.error("Direct LLM RestClient error: {}", e.getMessage(), e);
            throw new RuntimeException("LLM Gateway 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public String analyzePdf(Resource pdfResource, String query) throws Exception {
        String context = extractTextFromPdf(pdfResource);
        return analyzeText(context);
    }
}
