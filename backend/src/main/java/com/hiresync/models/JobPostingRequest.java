package com.hiresync.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobPostingRequest {
    @NotBlank(message = "회사명은 필수 입력 항목입니다.")
    private String companyName;

    @NotBlank(message = "근무 지역은 필수 입력 항목입니다.")
    private String location;

    @NotBlank(message = "채용 직무는 필수 입력 항목입니다.")
    private String position;

    private String requirements;
    private String openingDate;
    private String closingDate;
    private String applicationMethod;
    private String expectedSalary;
    private String status;
}
