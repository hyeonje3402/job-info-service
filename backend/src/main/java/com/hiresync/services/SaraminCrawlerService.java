package com.hiresync.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SaraminCrawlerService {

    public String crawlJobPosting(String url) {
        // 1. Chrome 브라우저를 Headless 모드로 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        StringBuilder resultText = new StringBuilder();

        try {
            System.out.println("URL 접속 중: " + url);
            driver.get(url);

            // 2. JavaScript 렌더링 완료 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // iframe 처리
            try {
                WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iframe_content_0")));
                driver.switchTo().frame(iframe);
            } catch (Exception e) {
                // iframe이 없는 경우 무시하고 진행
            }

            // 3. 페이지 본문 텍스트 데이터 추출
            WebElement bodyElement = driver.findElement(By.tagName("body"));
            resultText.append(bodyElement.getText());

        } catch (Exception e) {
            resultText.append("크롤링 중 오류 발생: ").append(e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        
        return resultText.toString();
    }

    // 간단한 실행 테스트를 위한 메인 메소드
    public static void main(String[] args) {
        // WebDriver 경로 설정이 필요할 수 있습니다 (환경에 따라 다름)
        // System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
        
        SaraminCrawlerService crawler = new SaraminCrawlerService();
        String targetUrl = "https://www.saramin.co.kr/zf_user/jobs/relay/view?view_type=list&rec_idx=53510601&t_ref=jobcategory_recruit&t_ref_content=general#seq=0";
        crawler.crawlJobPosting(targetUrl);
    }
}
