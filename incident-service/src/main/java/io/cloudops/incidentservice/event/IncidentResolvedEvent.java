package io.cloudops.incidentservice.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResolvedEvent {
    private Long incidentId;
    private String title;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
}
