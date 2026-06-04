package com.hiresync.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private Long id;
    private String companyName;
    private String position;
    private String location;
    private String requirements;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private String applicationMethod;
    private String expectedSalary;
    private String status;
    private SourceInfo source;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        private String status;
    }

    public static JobResponse fromEntity(JobPosting entity) {
        if (entity == null) return null;
        return JobResponse.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .position(entity.getPosition())
                .location(entity.getLocation())
                .requirements(entity.getRequirements())
                .openingDate(entity.getOpeningDate())
                .closingDate(entity.getClosingDate())
                .applicationMethod(entity.getApplicationMethod())
                .expectedSalary(entity.getExpectedSalary())
                .status(entity.getStatus())
                .source(entity.getSource() != null ? new SourceInfo(entity.getSource().getStatus()) : null)
                .build();
    }
}
