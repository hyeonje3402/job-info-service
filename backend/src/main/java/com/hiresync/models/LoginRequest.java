package com.hiresync.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Schema(description = "User's email address", example = "admin@example.com")
    private String email;

    @Schema(description = "User's password", example = "1234")
    private String password;
}
