package com.hiresync;

import com.hiresync.models.User;
import com.hiresync.services.UserService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HiresyncApplication {
    public static void main(String[] args) {
        // 프로젝트 루트 또는 현재 디렉토리에서 .env 파일을 시도합니다.
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        // 현재 디렉토리에 없고 상위 디렉토리에 있는 경우 (예: backend 폴더에서 실행 시) 재시도
        if (dotenv.get("OPENAI_API_KEY") == null) {
            dotenv = Dotenv.configure()
                    .directory("../")
                    .ignoreIfMissing()
                    .load();
        }

        // 로드된 변수를 시스템 프로퍼티로 설정하여 Spring Boot가 ${...} 문법으로 읽을 수 있게 합니다.
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(HiresyncApplication.class, args);
    }
}
