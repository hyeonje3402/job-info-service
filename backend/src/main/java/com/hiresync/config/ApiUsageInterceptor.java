package com.hiresync.config;

import com.hiresync.models.ApiUsageLog;
import com.hiresync.repositories.ApiUsageLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDateTime;

@Component
public class ApiUsageInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiUsageLogRepository apiUsageLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 다중 프록시 환경의 경우 첫 번째 IP가 실제 클라이언트 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();

        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            username = authentication.getName();
        }

        ApiUsageLog log = ApiUsageLog.builder()
                .ipAddress(ip != null ? ip : "unknown")
                .httpMethod(method)
                .requestUri(uri)
                .username(username)
                .createdAt(LocalDateTime.now())
                .build();

        apiUsageLogRepository.save(log);
        return true;
    }
}
