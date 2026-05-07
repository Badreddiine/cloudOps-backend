package io.cloudops.reportingservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {
    private long   totalOpen;
    private long   totalResolved;
    private double slaComplianceRate;
    private double avgResolutionTimeMinutes;
}
