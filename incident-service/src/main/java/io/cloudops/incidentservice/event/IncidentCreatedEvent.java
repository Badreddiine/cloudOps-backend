package io.cloudops.incidentservice.event;

import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.entity.IncidentPriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentCreatedEvent {
    private Long incidentId;
    private String title;
    private String reportedBy;
    private LocalDateTime createdAt;

    public Incident toEntity() {
        return Incident.builder()
                .id(this.incidentId)
                .title(this.title)
                .description("")           // not carried in event, set default
                .reportedBy(this.reportedBy)
                .status(IncidentStatus.OPEN)
                .priority(IncidentPriority.MEDIUM)
                .slaBreached(false)
                .createdAt(this.createdAt)
                .build();
    }
}