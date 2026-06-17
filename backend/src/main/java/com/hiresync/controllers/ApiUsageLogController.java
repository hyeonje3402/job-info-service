package com.hiresync.controllers;

import com.hiresync.models.ApiResponse;
import com.hiresync.models.ApiUsageLog;
import com.hiresync.services.ApiUsageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/api-logs")
public class ApiUsageLogController {

    @Autowired
    private ApiUsageLogService apiUsageLogService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ApiResponse<Page<ApiUsageLog>> getApiLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        Sort sortSpecification = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String field = parts[0];
            String direction = parts[1];
            if ("asc".equalsIgnoreCase(direction)) {
                sortSpecification = Sort.by(Sort.Direction.ASC, field);
            } else {
                sortSpecification = Sort.by(Sort.Direction.DESC, field);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sortSpecification);
        Page<ApiUsageLog> logPage = apiUsageLogService.getApiUsageLogs(pageable);
        return ApiResponse.success(logPage);
    }
}
