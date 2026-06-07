package com.hiresync.services;

import com.hiresync.models.ApiUsageLog;
import com.hiresync.repositories.ApiUsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiUsageLogService {

    @Autowired
    private ApiUsageLogRepository apiUsageLogRepository;

    @Transactional(readOnly = true)
    public Page<ApiUsageLog> getApiUsageLogs(Pageable pageable) {
        return apiUsageLogRepository.findAll(pageable);
    }
}
