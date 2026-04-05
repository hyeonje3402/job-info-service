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

    public String analyzePdf(Resource pdfResource, String query) {
        // 1. Tika를 사용하여 문서 읽기
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> documents = reader.get();
        log.info("Read {} document parts from PDF", documents.size());

        // 2. 텍스트 분할
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocs = splitter.apply(documents);

        // 3. 컨텍스트 결합
        String context = splitDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        
        if (context == null || context.trim().isEmpty()) {
            log.warn("Extracted text from PDF is empty");
            return "{\"jobPostings\": []}";
        }
        
        log.info("Extracted text length: {}", context.length());

        // 4. AI 질의
        return chatClient.prompt()
                .user(u -> u.text(extractionPrompt).param("context", context))
                .call()
                .content();
    }
}
