package io.cloudops.incidentservice.sla;

import io.cloudops.incidentservice.entity.IncidentPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlaStrategyFactory {

    private final CriticalSlaStrategy criticalStrategy;
    private final HighSlaStrategy     highStrategy;
    private final MediumSlaStrategy   mediumStrategy;
    private final LowSlaStrategy      lowStrategy;

    public SlaStrategy getStrategy(IncidentPriority priority) {
        return switch (priority) {
            case CRITICAL -> criticalStrategy;
            case HIGH     -> highStrategy;
            case MEDIUM   -> mediumStrategy;
            case LOW      -> lowStrategy;
        };
    }
}
