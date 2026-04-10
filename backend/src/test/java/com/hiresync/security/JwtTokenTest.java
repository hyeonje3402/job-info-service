package com.hiresync.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenTest {

    private JwtUtils jwtUtils;
    private final String secret = "v9y$B&E)H@McQfTjWnZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVkYp";
    private final int expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.setJwtSecret(secret);
        jwtUtils.setJwtExpirationMs(expiration);
    }

    @Test
    @DisplayName("shouldGenerateValidToken: 정상적인 토큰 생성 및 검증")
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
    @DisplayName("validateJwtToken: 유효하지 않은 서명인 경우 false를 반환한다 (SecurityException)")
    void shouldReturnFalseForInvalidSignature() {
        // Given: 다른 비밀키로 서명된 토큰 생성 (HS512를 위해 64바이트 이상의 키 필요)
        String longSecret = "differentSecretKeyForTestingOnly_MustBeLongEnoughForHS512_AtLeast64BytesLong!!";
        Key otherKey = Keys.hmacShaKeyFor(longSecret.getBytes());
        String tokenWithWrongKey = Jwts.builder()
                .setSubject("admin")
                .signWith(otherKey, SignatureAlgorithm.HS512)
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(tokenWithWrongKey);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: 구조가 깨진 토큰인 경우 false를 반환한다 (MalformedJwtException)")
    void shouldReturnFalseForMalformedToken() {
        // Given
        String malformedToken = "not.a.jwt";

        // When
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: 만료된 토큰인 경우 false를 반환한다 (ExpiredJwtException)")
    void shouldReturnFalseForExpiredToken() {
        // Given
        jwtUtils.setJwtExpirationMs(-1000); // 1초 전 만료되도록 설정
        String token = jwtUtils.generateJwtToken("admin");

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: 지원하지 않는 JWT 형식인 경우 false를 반환한다 (UnsupportedJwtException)")
    void shouldReturnFalseForUnsupportedToken() {
        // Given: 서명되지 않은 토큰(Plain JWT) 생성
        String unsupportedToken = Jwts.builder()
                .setSubject("admin")
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(unsupportedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken: 클레임 문자열이 비어있는 경우 false를 반환한다 (IllegalArgumentException)")
    void shouldReturnFalseForEmptyClaims() {
        // When
        boolean isNullValid = jwtUtils.validateJwtToken(null);
        boolean isEmptyValid = jwtUtils.validateJwtToken("");

        // Then
        assertThat(isNullValid).isFalse();
        assertThat(isEmptyValid).isFalse();
    }
}
