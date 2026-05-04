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
public class SlaBreachEvent {
    private Long incidentId;
    private String title;
    private LocalDateTime slaDeadline;
    private LocalDateTime breachedAt;
}
