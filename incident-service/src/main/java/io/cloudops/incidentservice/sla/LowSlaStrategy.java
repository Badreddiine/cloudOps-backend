package io.cloudops.incidentservice.sla;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LowSlaStrategy implements SlaStrategy {
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        return createdAt.plusHours(72);
    }
    public long getResponseTimeMinutes()  { return 480; }
    public long getResolutionTimeMinutes(){ return 4320; }
}
