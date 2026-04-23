package com.hiresync.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobPostingRequest {
    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;
    
    private String location;
    
    @NotBlank(message = "직무는 필수입니다.")
    private String position;
    
    private String requirements;
    private String openingDate;
    private String closingDate;
    private String applicationMethod;
    private String expectedSalary;
    private String status;
}
