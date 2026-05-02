package io.cloudops.incidentservice.dto.response;

import io.cloudops.incidentservice.entity.IncidentCategory;
import io.cloudops.incidentservice.entity.IncidentPriority;
import io.cloudops.incidentservice.entity.IncidentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IncidentResponse {
    private Long id;
    private String title;
    private String description;
    private IncidentStatus status;
    private IncidentPriority priority;
    private IncidentCategory category;
    private String assignedTo;
    private String reportedBy;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;
    private Boolean slaBreached;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AuditLogResponse> auditLogs;
}
