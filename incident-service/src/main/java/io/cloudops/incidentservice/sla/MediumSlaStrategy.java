package io.cloudops.incidentservice.sla;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MediumSlaStrategy implements SlaStrategy {
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        return createdAt.plusHours(24);
    }
    public long getResponseTimeMinutes()  { return 120; }
    public long getResolutionTimeMinutes(){ return 1440; }
}
