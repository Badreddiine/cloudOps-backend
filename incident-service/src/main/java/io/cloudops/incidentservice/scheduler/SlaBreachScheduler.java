package io.cloudops.incidentservice.scheduler;

import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.repository.IncidentRepository;
import io.cloudops.incidentservice.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaBreachScheduler {
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    // Exécute toutes les 5 minutes
    @Scheduled(fixedRate = 300000) // 300000 ms = 5 minutes
    public void checkSlaBreaches() {
        log.info("Checking for SLA breaches...");
        List<Incident> incidents = incidentRepository.findAll(); // Peut être optimisé avec une requête

        incidents.stream()
                .filter(incident -> !incident.getSlaBreached() && incident.getSlaDeadline() != null &&
                        incident.getSlaDeadline().isBefore(LocalDateTime.now()))
                .forEach(incident -> {
                    incidentService.markSlaBreached(incident.getId());
                    log.warn("SLA breached for incident ID: {}", incident.getId());
// TODO: Trigger Kafka event for SLA breach
                });
        log.info("SLA breach check completed.");
    }
}
