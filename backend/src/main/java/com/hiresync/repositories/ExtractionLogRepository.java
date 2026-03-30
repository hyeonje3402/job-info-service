package com.hiresync.repositories;

import com.hiresync.models.ExtractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractionLogRepository extends JpaRepository<ExtractionLog, Long> {
}
