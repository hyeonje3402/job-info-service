package com.hiresync.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisService.class);
    private final ChatClient chatClient;

    @Value("classpath:prompts/job-extraction.st")
    private Resource extractionPrompt;

    public PdfAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Extracts raw text from a PDF resource.
     */
    public String extractTextFromPdf(Resource pdfResource) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
            List<Document> documents = reader.get();
            log.info("Read {} document parts from PDF", documents.size());

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocs = splitter.apply(documents);

            String context = splitDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));

            if (context == null || context.trim().isEmpty()) {
                log.warn("Extracted text from PDF is empty");
                return "";
            }

            log.info("Extracted text length: {}", context.length());
            return context;
        } catch (Exception e) {
            log.error("Error extracting text from PDF: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Sends the provided text to AI to extract job details.
     */
    public String analyzeText(String context) {
        if (context == null || context.trim().isEmpty()) {
            return "{\"jobPostings\": []}";
        }
        
        try {
            return chatClient.prompt()
                    .user(u -> u.text(extractionPrompt).param("context", context))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Error analyzing text with AI: {}", e.getMessage());
            return "{\"jobPostings\": []}";
        }
    }

    /**
     * Combined method for backward compatibility if needed.
     */
    public String analyzePdf(Resource pdfResource, String query) {
        String context = extractTextFromPdf(pdfResource);
        return analyzeText(context);
    }
}
