package com.hiresync.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfAnalysisService {

    private final ChatClient chatClient;

    public PdfAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzePdf(Resource pdfResource, String query) {
        // 1. Tika를 사용하여 문서 읽기 (PDF 등 다양한 형식 지원)
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> documents = reader.get();

        // 2. 텍스트 분할
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocs = splitter.apply(documents);

        // 3. 컨텍스트 결합
        String context = splitDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 4. AI 질의
        return chatClient.prompt()
                .system("당신은 채용 공고 분석 전문가입니다. 제공된 문서 내용을 바탕으로 다음 JSON 형식으로만 답변하세요. " +
                        "필드: companyName, location, field, conditions, deadline, method, expectedSalary. " +
                        "JSON 이외의 텍스트는 포함하지 마세요.")
                .user(u -> u.text(String.format("문서 내용:\n%s\n\n질문: %s", context, query)))
                .call()
                .content();
    }
}
