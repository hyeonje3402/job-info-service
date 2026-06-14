package com.hiresync.controllers;

import com.hiresync.models.ApiResponse;
import com.hiresync.services.JobImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobImportController {

    private final JobImportService jobImportService;

    public JobImportController(JobImportService jobImportService) {
        this.jobImportService = jobImportService;
    }

    @PostMapping("/jobs/import")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<?>> importJob(@RequestBody Map<String, String> request) {
        if (request == null || !request.containsKey("url") || request.get("url") == null || request.get("url").isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "URL은 필수 입력값입니다."));
        }

        String url = request.get("url");

        // SSRF 검증 수행 (Service를 통해서도 검증하지만 컨트롤러에서도 400 Bad Request를 내려주기 위해 사전에 체크)
        if (!jobImportService.isSafeUrl(url)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", "허용되지 않거나 접근 불가능한 URL 경로입니다."));
        }

        try {
            Map<String, Object> crawlData = jobImportService.importJobFromUrl(url);
            return ResponseEntity.ok(ApiResponse.success(crawlData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "크롤링 처리 중 서버 에러가 발생했습니다: " + e.getMessage()));
        }
    }
}
