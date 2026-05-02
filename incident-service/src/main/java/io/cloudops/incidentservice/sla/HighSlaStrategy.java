package io.cloudops.incidentservice.sla;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HighSlaStrategy implements SlaStrategy {
    public LocalDateTime calculateDeadline(LocalDateTime createdAt){
        return createdAt.plusHours(4);
    }
    public long getResponseTimeMinutes() {
        return 30;
    }
    public long getResolutionTimeMinutes() {
        return 240;
    }
}
