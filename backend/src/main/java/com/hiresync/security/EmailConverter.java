package com.hiresync.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Base64;

@Converter
public class EmailConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        // Simple Base64 for demonstration; use AES for production.
        return Base64.getEncoder().encodeToString(attribute.getBytes());
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return new String(Base64.getDecoder().decode(dbData));
        } catch (IllegalArgumentException e) {
            return dbData; // Return as-is if not encoded
        }
    }
}
