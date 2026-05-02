package io.cloudops.incidentservice.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Builder
public class SlaBreachEvent {
    private Long incidentId;
    private String title;
    private LocalDateTime slaDeadline;
    private LocalDateTime breachTime;
}
