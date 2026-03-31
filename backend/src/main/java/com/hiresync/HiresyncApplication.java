package com.hiresync;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HiresyncApplication {
    public static void main(String[] args) {
        // 프로젝트 루트 디렉토리에서 .env 파일을 로드합니다.
        Dotenv dotenv = Dotenv.configure()
                .directory("../") // backend가 하위 디렉토리이므로 상위로 이동
                .ignoreIfMissing()
                .load();
        
        // 로드된 변수를 시스템 프로퍼티로 설정하여 Spring Boot가 읽을 수 있게 합니다.
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(HiresyncApplication.class, args);
    }
}
