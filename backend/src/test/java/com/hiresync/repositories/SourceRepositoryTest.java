package com.hiresync.repositories;

import com.hiresync.models.Source;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SourceRepositoryTest {

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    void shouldSaveAndFindSource() {
        // Given
        Source source = new Source();
        source.setType("URL");
        source.setReferenceUri("http://example.com/job");
        source.setStatus("SUCCESS");
        sourceRepository.save(source);

        // When
        Optional<Source> found = sourceRepository.findById(source.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("URL");
        assertThat(found.get().getReferenceUri()).isEqualTo("http://example.com/job");
    }
}
