package io.cloudops.incidentservice.factory;

import io.cloudops.incidentservice.dto.request.CreateIncidentRequest;
import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.sla.SlaStrategy;
import io.cloudops.incidentservice.sla.SlaStrategyFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Data
public class IncidentFactory {

    private final SlaStrategyFactory slaStrategyFactory;

    public Incident create(CreateIncidentRequest request, String reportedBy) {
        SlaStrategy strategy = slaStrategyFactory.getStrategy(request.getPriority());
        LocalDateTime now = LocalDateTime.now();

        return Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .assignedTo(request.getAssignedTo())
                .reportedBy(reportedBy)
                .status(IncidentStatus.OPEN)
                .slaDeadline(strategy.calculateDeadline(now))  // ← SLA calculé ici
                .slaBreached(false)
                .build();
    }
}