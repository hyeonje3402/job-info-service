package com.hiresync.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenTest {

    private JwtUtils jwtUtils;
    private String secret = "v9y$B&E)H@McQfTjWnZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVkYp";
    private long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.setJwtSecret(secret);
        jwtUtils.setJwtExpirationMs((int) expiration);
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        String username = "admin";

        // When
        String token = jwtUtils.generateJwtToken(username);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo(username);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        // Given
        jwtUtils.setJwtExpirationMs(-1000); // Already expired
        String token = jwtUtils.generateJwtToken("admin");

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isFalse();
    }
}
