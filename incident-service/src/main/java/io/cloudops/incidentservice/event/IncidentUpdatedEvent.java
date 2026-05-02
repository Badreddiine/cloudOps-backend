package io.cloudops.incidentservice.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentUpdatedEvent {
    private Long incidentId;
    private String title;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String oldStatus;
    private String newStatus;
}
