package io.cloudops.incidentservice.dto.response;

import io.cloudops.incidentservice.entity.IncidentCategory;
import io.cloudops.incidentservice.entity.IncidentPriority;
import io.cloudops.incidentservice.entity.IncidentStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class IncidentSummaryResponse {
    private Long id;
    private String title;
    private IncidentStatus status;
    private IncidentPriority priority;
    private IncidentCategory category;
    private String assignedTo;
    private String reportedBy;
    private LocalDateTime slaDeadline;
    private Boolean slaBreached;
    private LocalDateTime createdAt;
}
