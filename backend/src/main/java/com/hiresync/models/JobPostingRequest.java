package com.hiresync.models;

import lombok.Data;

@Data
public class JobPostingRequest {
    private String companyName;
    private String location;
    private String position;
    private String requirements;
    private String openingDate;
    private String closingDate;
    private String applicationMethod;
    private String expectedSalary;
    private String status;
}
