package io.cloudops.reportingservice.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
    private String             month;
    private long               total;
    private long               criticalCount;
    private long               highCount;
    private long               mediumCount;
    private long               lowCount;
    private long               resolvedCount;
    private long               slaBreachedCount;
    private double             avgResolutionMinutes;
    private double             slaComplianceRate;
    private List<ReportRowDTO> incidents;
}
