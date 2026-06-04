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
            // 1. URL 접속 및 문서 로드
            Document doc = Jsoup.connect(url)
                    .timeout(30000) // 타임아웃을 30초로 증가 (기존 10초에서 변경)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .followRedirects(true) // 리다이렉트 허용
                    .referrer("http://www.google.com") // 참조 페이지 설정 (봇 탐지 우회 도움)
                    .get();

            // 2. HTML 태그 제거 및 안전한 텍스트 추출 (JSoup Cleaner)
            String cleanHtml = Jsoup.clean(doc.body().html(), Safelist.none());
            
            // 3. 텍스트 추출 및 공백 정리
            String text = Jsoup.parse(cleanHtml).text();
            
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
