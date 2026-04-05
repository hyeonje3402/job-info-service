package com.hiresync.services;

import lombok.extern.slf4j.Slf4j;
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
                    .timeout(10000) // SC-002: 10초 타임아웃
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();

            // 2. HTML 태그 제거 및 안전한 텍스트 추출 (JSoup Cleaner)
            String cleanHtml = Jsoup.clean(doc.body().html(), Safelist.none());
            
            // 3. 텍스트 추출 및 공백 정리
            String text = Jsoup.parse(cleanHtml).text();
            
            log.info("Successfully scraped text from {}. Length: {}", url, text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to scrape URL: {}", url, e);
            throw new RuntimeException("URL 스크래핑 실패: " + e.getMessage());
        }
    }
}
