package com.hiresync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresync.services.JobImportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobImportService jobImportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("importJob: 권한이 없는 일반 사용자가 요청 시 403 Forbidden 반환")
    @WithMockUser(authorities = "GENERAL")
    void shouldDenyAccessForNonAdminUser() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", "https://example.com/jobs");

        mockMvc.perform(post("/api/jobs/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("importJob: ADMIN 권한으로 안전하지 않은 URL(SSRF 가능성) 전달 시 400 Bad Request 반환")
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnBadRequestForUnsafeUrl() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", "http://localhost:8080/sensitive");

        // mock된 서비스에서 isSafeUrl이 false를 리턴하게 설정
        when(jobImportService.isSafeUrl("http://localhost:8080/sensitive")).thenReturn(false);

        mockMvc.perform(post("/api/jobs/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("허용되지 않거나 접근 불가능한 URL 경로입니다."));
    }

    @Test
    @DisplayName("importJob: ADMIN 권한으로 올바른 외부 URL 전달 시 크롤링 성공 데이터를 200 OK와 함께 반환")
    @WithMockUser(authorities = "ADMIN")
    void shouldSuccessfullyCrawlValidUrl() throws Exception {
        String testUrl = "https://naver.com/jobs";
        Map<String, String> request = new HashMap<>();
        request.put("url", testUrl);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("title", "Naver Career");
        mockResult.put("heading", "Naver Developer Openings");
        mockResult.put("url", testUrl);
        mockResult.put("status", "SUCCESS");

        when(jobImportService.isSafeUrl(testUrl)).thenReturn(true);
        when(jobImportService.importJobFromUrl(testUrl)).thenReturn(mockResult);

        mockMvc.perform(post("/api/jobs/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("Naver Career"))
                .andExpect(jsonPath("$.data.heading").value("Naver Developer Openings"));
    }
}
