package io.cloudops.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentUpdatedEvent {
    private Long incidentId;
    private String title;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String oldStatus;
    private String newStatus;
}
