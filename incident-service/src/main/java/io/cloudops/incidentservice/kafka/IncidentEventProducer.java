package io.cloudops.incidentservice.kafka;

import io.cloudops.event.IncidentCreatedEvent;
import io.cloudops.event.IncidentResolvedEvent;
import io.cloudops.event.IncidentUpdatedEvent;
import io.cloudops.event.SlaBreachEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC_CREATED  = "incident.created";
    public static final String TOPIC_UPDATED  = "incident.updated";
    public static final String TOPIC_RESOLVED = "incident.resolved";
    public static final String TOPIC_SLA      = "incident.sla.breach";

    // ─── Producers avec Circuit Breaker ──────────────────────────────────────

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackCreated")
    public void sendIncidentCreated(IncidentCreatedEvent event) {
        kafkaTemplate.send(TOPIC_CREATED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_CREATED, event.getIncidentId());
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackUpdated")
    public void sendIncidentUpdated(IncidentUpdatedEvent event) {
        kafkaTemplate.send(TOPIC_UPDATED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_UPDATED, event.getIncidentId());
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackResolved")
    public void sendIncidentResolved(IncidentResolvedEvent event) {
        kafkaTemplate.send(TOPIC_RESOLVED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_RESOLVED, event.getIncidentId());
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSla")
    public void sendSlaBreachEvent(SlaBreachEvent event) {
        kafkaTemplate.send(TOPIC_SLA, event.getIncidentId().toString(), event);
        log.warn("SLA BREACH Event sent -> {} : incidentId={}", TOPIC_SLA, event.getIncidentId());
    }

    // ─── Fallbacks ────────────────────────────────────────────────────────────

    public void fallbackCreated(IncidentCreatedEvent event, Exception e) {
        log.error("[CB] Kafka unavailable - CREATED event lost for incidentId={} : {}",
                event.getIncidentId(), e.getMessage());
    }

    public void fallbackUpdated(IncidentUpdatedEvent event, Exception e) {
        log.error("[CB] Kafka unavailable - UPDATED event lost for incidentId={} : {}",
                event.getIncidentId(), e.getMessage());
    }

    public void fallbackResolved(IncidentResolvedEvent event, Exception e) {
        log.error("[CB] Kafka unavailable - RESOLVED event lost for incidentId={} : {}",
                event.getIncidentId(), e.getMessage());
    }

    public void fallbackSla(SlaBreachEvent event, Exception e) {
        log.error("[CB] Kafka unavailable - SLA BREACH event lost for incidentId={} : {}",
                event.getIncidentId(), e.getMessage());
    }
}
