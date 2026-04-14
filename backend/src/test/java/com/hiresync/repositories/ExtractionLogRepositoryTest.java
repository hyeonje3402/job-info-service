package com.hiresync.repositories;

import com.hiresync.models.ExtractionLog;
import com.hiresync.models.Source;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExtractionLogRepositoryTest {

    @Autowired
    private ExtractionLogRepository extractionLogRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    void shouldSaveAndFindExtractionLog() {
        // Given
        Source source = new Source();
        source.setType("PDF");
        source.setReferenceUri("test.pdf");
        source.setStatus("SUCCESS");
        sourceRepository.save(source);

        ExtractionLog log = new ExtractionLog();
        log.setSource(source);
        log.setModelVersion("gpt-3.5-turbo");
        log.setRawData("{}");
        log.setProcessingTimeMs(100);
        extractionLogRepository.save(log);

        // When
        Optional<ExtractionLog> found = extractionLogRepository.findById(log.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getModelVersion()).isEqualTo("gpt-3.5-turbo");
    }
}
