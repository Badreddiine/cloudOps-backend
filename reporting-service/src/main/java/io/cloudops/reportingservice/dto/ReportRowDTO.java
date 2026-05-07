package io.cloudops.reportingservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRowDTO {
    private Long          incidentId;
    private String        title;
    private String        priority;
    private String        status;
    private String        serviceImpacted;
    private String        teamName;
    private String        assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long          resolutionTimeMinutes;
    private boolean       slaBreached;
}
