package io.cloudops.incidentservice.event;

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
}
