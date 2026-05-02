package io.cloudops.incidentservice.sla;

import java.time.LocalDateTime;
//Strategy Pattern — SLA
public interface SlaStrategy {
    LocalDateTime calculateDeadline(LocalDateTime createdAt);
    long getResponseTimeMinutes();
    long getResolutionTimeMinutes();
}
