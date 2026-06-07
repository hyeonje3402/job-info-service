package com.hiresync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ApiUsageInterceptor apiUsageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /api/v1/** 로 시작하는 모든 API 호출 경로에 매핑
        registry.addInterceptor(apiUsageInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
