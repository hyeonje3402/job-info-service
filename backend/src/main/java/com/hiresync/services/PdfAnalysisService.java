package com.hiresync.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PdfAnalysisService.class);
    private final ChatClient chatClient;

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
        // json_object 형식을 위해 root를 객체로 정의
        return chatClient.prompt()
                .system("당신은 채용 공고 분석 전문가입니다. 제공된 문서 내용을 바탕으로 " +
                        "문서에 포함된 모든 채용 공고(기업별)를 추출하여 반드시 다음 JSON 객체 형식으로만 답변하세요. " +
                        "형식: {\"jobPostings\": [{\"companyName\": \"기업명\", \"location\": \"지역\", \"field\": \"분야\", \"conditions\": \"자격요건\", \"deadline\": \"마감일\", \"method\": \"접수방법\", \"expectedSalary\": \"예상급여\"}]} " +
                        "문서에 여러 기업의 공고가 있다면 모두 배열에 포함하세요. JSON 이외의 텍스트는 절대 포함하지 마세요.")
                .user(u -> u.text(String.format("문서 내용:\n%s\n\n질문: %s", context, query)))
                .call()
                .content();
    }
}
