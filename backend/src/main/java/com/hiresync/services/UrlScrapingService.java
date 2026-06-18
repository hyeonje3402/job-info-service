package com.hiresync.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UrlScrapingService {

    /**
     * URL에서 HTML을 스크래핑하고 텍스트만 추출 (SR-004 준수: JSoup Cleaner 사용)
     */
    public String scrapeUrl(String url) {
        try {
            String targetUrl = url;
            String saraminCompany = null;
            String saraminTitle = null;

            if (url != null && url.contains("saramin.co.kr")) {
                // 1) 메인 페이지에서 회사명 및 공고제목을 먼저 긁어본다
                try {
                    Document mainDoc = Jsoup.connect(url)
                            .timeout(15000)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .followRedirects(true)
                            .referrer("http://www.google.com")
                            .get();

                    // meta[name=description] 에서 회사명 추출 시도
                    String metaDesc = mainDoc.select("meta[name=description]").attr("content");
                    if (metaDesc != null && !metaDesc.isBlank()) {
                        String[] parts = metaDesc.split(",");
                        if (parts.length > 0) {
                            saraminCompany = parts[0].trim();
                        }
                    }

                    // og:title 또는 doc.title 에서 제목 추출 시도
                    String ogTitle = mainDoc.select("meta[property=og:title]").attr("content");
                    if (ogTitle != null && !ogTitle.isBlank()) {
                        saraminTitle = ogTitle.replace(" - 사람인", "").trim();
                    } else {
                        saraminTitle = mainDoc.title().replace(" - 사람인", "").trim();
                    }

                    // 만약 metaDesc가 비어있거나 회사명이 안 찾아졌다면 title 대괄호에서 추출
                    if (saraminCompany == null || saraminCompany.isBlank()) {
                        String titleText = ogTitle != null && !ogTitle.isBlank() ? ogTitle : mainDoc.title();
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[([^\\]]+)\\]");
                        java.util.regex.Matcher m = p.matcher(titleText);
                        if (m.find()) {
                            saraminCompany = m.group(1).trim();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract metadata from main Saramin page", e);
                }

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[?&]rec_idx=([^&#]+)");
                java.util.regex.Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String recIdx = matcher.group(1);
                    targetUrl = "https://www.saramin.co.kr/zf_user/jobs/relay/view-detail?rec_idx=" + recIdx;
                    log.info("Converted Saramin URL to view-detail: {} -> {}", url, targetUrl);
                }
            }

            // 1. URL 접속 및 문서 로드
            Document doc = Jsoup.connect(targetUrl)
                    .timeout(30000) // 타임아웃을 30초로 증가 (기존 10초에서 변경)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .followRedirects(true) // 리다이렉트 허용
                    .referrer("http://www.google.com") // 참조 페이지 설정 (봇 탐지 우회 도움)
                    .get();

            // 2. 불필요한 동적/스타일 태그 사전에 제거
            doc.select("script, style, iframe, noscript").remove();

            // 3. HTML 태그 제거 및 안전한 텍스트 추출 (JSoup Cleaner - SR-004 준수)
            String cleanHtml = Jsoup.clean(doc.body().html(), Safelist.none());
            
            // 4. 텍스트 추출 및 공백 정리
            String text = Jsoup.parse(cleanHtml).text();
            
            if (saraminCompany != null || saraminTitle != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("[채용 기본 정보]\n");
                if (saraminCompany != null && !saraminCompany.isBlank()) {
                    sb.append("회사명: ").append(saraminCompany).append("\n");
                }
                if (saraminTitle != null && !saraminTitle.isBlank()) {
                    sb.append("공고제목: ").append(saraminTitle).append("\n");
                }
                sb.append("\n[상세 요강]\n");
                sb.append(text);
                text = sb.toString();
            }

            // 디버깅용 텍스트 덤프 저장
            try {
                java.nio.file.Files.writeString(
                    java.nio.file.Path.of("/home/hjj/projects/github-hyeonje3402/scratch/scraped_text.txt"),
                    text
                );
            } catch (Exception ignored) {}
            
            log.info("Successfully scraped text from {}. Length: {}", url, text.length());
            return text;
        } catch (HttpStatusException e) {
            log.error("HTTP error fetching URL: {}. Status: {}, Message: {}", url, e.getStatusCode(), e.getMessage());
            throw new IllegalArgumentException("URL 접근 오류 (" + e.getStatusCode() + "): 해당 페이지에 접근할 수 없거나 존재하지 않습니다.");
        } catch (IOException e) {
            log.error("IO error fetching URL: {}", url, e);
            throw new RuntimeException("URL 연결 실패: " + e.getMessage());
        }
    }
}
