package com.hiresync.repositories;

import com.hiresync.models.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void shouldSaveAndFindByUsername() {
        // Given
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("password");
        admin.setEmail("admin@example.com");
        adminRepository.save(admin);

        // When
        Optional<Admin> found = adminRepository.findByUsername("admin");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("admin@example.com");
    }
}
