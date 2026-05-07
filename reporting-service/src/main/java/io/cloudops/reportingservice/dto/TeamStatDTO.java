package io.cloudops.reportingservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatDTO {
    private String teamName;
    private long   total;
    private long   slaBreached;
    private double slaCompliancePercent;
}
