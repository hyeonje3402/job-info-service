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

import java.io.InputStream;
import java.util.Map;

@Service
public class PdfAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisService.class);
    private final ChatClient chatClient;

    private final String promptText = """
        You are a world-class job posting analyzer.
        Extract ALL individual job recruitment information from the provided text into a JSON array.
        
        [STRICT INSTRUCTIONS]
        1. Find company names even if the text is messy.
        2. Convert deadlines to 'YYYY-MM-DD'.
        3. If any field information (like opening date) is NOT explicitly mentioned in the text, you MUST set its value to null. (NEVER hallucinate or use today's date).
        4. If there are multiple locations, list ALL of them separated by commas (e.g., "Icheon, Yongin, Cheongju").
        5. Include even expired or old job postings.
        6. Provide the response ONLY as a valid JSON object matching the structure below.
        7. DO NOT include any markdown tags (like ```json) or explanations.
        
        [JSON STRUCTURE]
        {json_structure}
        
        [TEXT]
        {pdf_content}
        """;

    private final String jsonStructure = """
        {
          "jobPostings": [
            {
              "companyName": "Company Name (null if missing)",
              "location": "List ALL locations separated by commas (null if missing)",
              "position": "Job Title (Required)",
              "requirements": "Qualifications (null if missing)",
              "openingDate": "YYYY-MM-DD (null if missing)",
              "closingDate": "YYYY-MM-DD (null if missing)",
              "applicationMethod": "Apply Link or Method (null if missing)",
              "expectedSalary": "Salary (null if missing)"
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
        String cleaned = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.trim();
    }

    public String analyzeText(String context) {
        if (context == null || context.isBlank()) return "{\"jobPostings\": []}";
        
        try {
            log.info("Starting AI analysis... (Text length: {})", context.length());
            
            // 템플릿 생성 및 변수 주입 (JSON 구조와 PDF 본문을 모두 파라미터로 주입)
            PromptTemplate template = new PromptTemplate(promptText);
            Prompt prompt = template.create(Map.of(
                    "json_structure", jsonStructure,
                    "pdf_content", context
            ));
            
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();
            
            if (response == null || response.isBlank()) {
                log.warn("AI returned empty response.");
                return "{\"jobPostings\": []}";
            }
            return response;
        } catch (Exception e) {
            log.error("AI analysis error: {}", e.getMessage(), e);
            return "{\"jobPostings\": []}";
        }
    }

    public String analyzePdf(Resource pdfResource, String query) {
        String context = extractTextFromPdf(pdfResource);
        return analyzeText(context);
    }
}
