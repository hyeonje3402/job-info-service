package com.hiresync.repositories;

import com.hiresync.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindByEmail() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnSave() {
        // Given
        User user = new User();
        user.setEmail("lifecycle@example.com");
        user.setPassword("password");
        user.setName("Lifecycle User");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUpdatedAtOnModification() throws InterruptedException {
        // Given
        User user = new User();
        user.setEmail("update@example.com");
        user.setPassword("password");
        user.setName("Update User");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        User saved = userRepository.saveAndFlush(user);
        java.time.LocalDateTime originalUpdatedAt = saved.getUpdatedAt();

        // Wait a bit to ensure a different timestamp
        Thread.sleep(10);

        // When
        saved.setName("Updated Name");
        User updated = userRepository.saveAndFlush(saved);

        // Then
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}
