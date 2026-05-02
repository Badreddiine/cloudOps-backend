package io.cloudops.incidentservice.sla;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CriticalSlaStrategy implements SlaStrategy {
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        return createdAt.plusHours(1);
    }
    public long getResponseTimeMinutes()  {
        return 15; }
    public long getResolutionTimeMinutes(){
        return 60;
    }
}
