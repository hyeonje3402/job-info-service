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
        1. Find company names like 'SK Key Foundry' even if the text is messy.
        2. Convert deadlines to 'YYYY-MM-DD'. If 'until filled', use '2026-12-31'.
        3. Opening date is today: 2026-04-06.
        4. Provide the response ONLY as a valid JSON object matching the structure below.
        5. DO NOT include any markdown tags (like ```json) or explanations.
        
        [JSON STRUCTURE]
        {json_structure}
        
        [TEXT]
        {pdf_content}
        """;

    private final String jsonStructure = """
        {
          "jobPostings": [
            {
              "companyName": "Company Name",
              "location": "Location (use 'TBD' if unknown)",
              "position": "Job Title (Required)",
              "requirements": "Qualifications",
              "openingDate": "2026-04-06",
              "closingDate": "YYYY-MM-DD",
              "applicationMethod": "Apply Link or Method",
              "expectedSalary": "Salary (use 'Negotiable' if unknown)"
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
