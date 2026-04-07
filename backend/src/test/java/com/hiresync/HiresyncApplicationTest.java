package com.hiresync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HiresyncApplicationTest {

    @Test
    void contextLoads() {
        // 애플리케이션 컨텍스트가 성공적으로 로드되는지 확인
    }

    @Test
    void testMain() {
        // main 메서드 내부의 Dotenv 로직 및 SpringApplication.run 호출 커버리지 향상
        assertDoesNotThrow(() -> {
            HiresyncApplication.main(new String[]{"--server.port=0"});
        });
    }
}
