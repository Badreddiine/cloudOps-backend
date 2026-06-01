package io.cloudops.incidentservice.scheduler;

import io.cloudops.event.SlaBreachEvent;
import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.kafka.IncidentEventProducer;
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
    private final IncidentService    incidentService;
    private final IncidentEventProducer eventProducer;

    @Scheduled(fixedRate = 300_000) // toutes les 5 minutes
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[SLA Scheduler] Starting SLA breach check at {}", now);

        List<Incident> breachedIncidents = incidentRepository.findPendingSlaBreaches(now);

        if (breachedIncidents.isEmpty()) {
            log.info("[SLA Scheduler] No SLA breaches detected.");
            return;
        }

        log.warn("[SLA Scheduler] {} incident(s) breached SLA deadline.", breachedIncidents.size());

        for (Incident incident : breachedIncidents) {
            try {
                incidentService.markSlaBreached(incident.getId());

                eventProducer.sendSlaBreachEvent(SlaBreachEvent.builder()
                        .incidentId(incident.getId())
                        .title(incident.getTitle())
                        .slaDeadline(incident.getSlaDeadline())
                        .breachedAt(now)
                        .build());

                log.warn("[SLA Scheduler] SLA breached & event sent for incidentId={}  deadline={}",
                        incident.getId(), incident.getSlaDeadline());

            } catch (Exception e) {
                log.error("[SLA Scheduler] Failed to process SLA breach for incidentId={} : {}",
                        incident.getId(), e.getMessage());
            }
        }

        log.info("[SLA Scheduler] SLA breach check completed.");
    }
}