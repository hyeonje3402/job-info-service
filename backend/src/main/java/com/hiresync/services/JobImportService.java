package com.hiresync.services;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class JobImportService {

    private static final Logger log = LoggerFactory.getLogger(JobImportService.class);

    public JobImportService() {
        // WebDriverManager를 사용해 ChromeDriver 셋업 진행
        WebDriverManager.chromedriver().setup();
    }

    public Map<String, Object> importJobFromUrl(String urlString) {
        // 1. SSRF 검증 진행
        if (!isSafeUrl(urlString)) {
            throw new IllegalArgumentException("허용되지 않거나 접근 불가능한 URL 경로입니다.");
        }

        Map<String, Object> result = new HashMap<>();
        WebDriver driver = null;

        try {
            // Chrome 실행 옵션 설정 (Headless 모드 필수)
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1280,1000");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));

            log.info("Selenium WebDriver를 사용하여 URL 접속 시도 중: {}", urlString);
            driver.get(urlString);

            // 뼈대 추출 로직: 기본 제목(Title) 추출
            String title = driver.getTitle();
            result.put("title", title != null ? title.trim() : "N/A");

            // 특정 H1 태그 값 수집 시도 (존재하지 않을 경우를 위한 예외 처리 내포)
            String heading = "N/A";
            try {
                WebElement h1Element = driver.findElement(By.tagName("h1"));
                if (h1Element != null) {
                    heading = h1Element.getText().trim();
                }
            } catch (Exception e) {
                log.warn("페이지 내 h1 태그를 찾을 수 없습니다: {}", e.getMessage());
            }
            result.put("heading", heading);
            result.put("url", urlString);
            result.put("status", "SUCCESS");

            log.info("크롤링 성공: Title = [{}], Heading = [{}]", title, heading);

        } catch (Exception e) {
            log.error("URL 크롤링 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("크롤링 중 서버 오류가 발생하였습니다: " + e.getMessage(), e);
        } finally {
            // DoS 및 메모리 누수 방지를 위한 드라이버 자원 해제 보장
            if (driver != null) {
                try {
                    driver.quit();
                    log.info("Selenium WebDriver가 성공적으로 리소스를 해제했습니다.");
                } catch (Exception ex) {
                    log.error("WebDriver 종료 중 예외 발생: {}", ex.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * SSRF (Server-Side Request Forgery) 방지를 위한 URL 안정성 검증
     */
    public boolean isSafeUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }

        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol();
            
            // HTTP 및 HTTPS 프로토콜만 승인
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                log.warn("SSRF 차단: 허용되지 않는 프로토콜 = {}", protocol);
                return false;
            }

            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            // 호스트 IP 주소 분석
            InetAddress address = InetAddress.getByName(host);

            // Loopback(127.x.x.x, localhost), LinkLocal(169.254.x.x), SiteLocal(10.x.x.x, 192.168.x.x, 172.16.x.x) 차단
            if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                log.warn("SSRF 차단: 내부 대역 주소 감지 = {} ({})", host, address.getHostAddress());
                return false;
            }

            return true;
        } catch (UnknownHostException e) {
            log.warn("SSRF 차단: 알 수 없는 호스트 정보 = {}", urlString);
            return false;
        } catch (Exception e) {
            log.error("URL 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
