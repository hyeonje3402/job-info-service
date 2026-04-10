package com.hiresync.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EmailConverterTest {

    private EmailConverter emailConverter;

    @BeforeEach
    void setUp() {
        emailConverter = new EmailConverter();
    }

    @Test
    @DisplayName("convertToDatabaseColumn: 평문 이메일을 Base64로 인코딩하여 반환한다")
    void convertToDatabaseColumn_ValidEmail_ReturnsBase64EncodedString() {
        // given
        String email = "test@example.com";
        String expectedBase64 = Base64.getEncoder().encodeToString(email.getBytes());

        // when
        String result = emailConverter.convertToDatabaseColumn(email);

        // then
        assertThat(result).isEqualTo(expectedBase64);
    }

    @Test
    @DisplayName("convertToDatabaseColumn: null이 입력되면 null을 반환한다")
    void convertToDatabaseColumn_NullInput_ReturnsNull() {
        // when
        String result = emailConverter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("convertToEntityAttribute: Base64 인코딩된 문자열을 평문으로 디코딩한다")
    void convertToEntityAttribute_ValidBase64_ReturnsDecodedString() {
        // given
        String email = "test@example.com";
        String dbData = Base64.getEncoder().encodeToString(email.getBytes());

        // when
        String result = emailConverter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("convertToEntityAttribute: 유효하지 않은 Base64 문자열이면 원본을 그대로 반환한다")
    void convertToEntityAttribute_InvalidBase64_ReturnsAsIs() {
        // given
        String invalidBase64 = "not-a-base64-string!";

        // when
        String result = emailConverter.convertToEntityAttribute(invalidBase64);

        // then
        assertThat(result).isEqualTo(invalidBase64);
    }

    @Test
    @DisplayName("convertToEntityAttribute: null이 입력되면 null을 반환한다")
    void convertToEntityAttribute_NullInput_ReturnsNull() {
        // when
        String result = emailConverter.convertToEntityAttribute(null);

        // then
        assertThat(result).isNull();
    }
}
