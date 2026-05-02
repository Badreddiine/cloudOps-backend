package io.cloudops.incidentservice.kafka;

import io.cloudops.incidentservice.event.IncidentCreatedEvent;
import io.cloudops.incidentservice.event.IncidentResolvedEvent;
import io.cloudops.incidentservice.event.IncidentUpdatedEvent;
import io.cloudops.incidentservice.event.SlaBreachEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String TOPIC_CREATED = "incident.created";
    public static final String TOPIC_UPDATED = "incident.updated";
    public static final String TOPIC_RESOLVED = "incident.resolved";
    public static final String TOPIC_SLA = "incident.sla.breach";
    public void sendIncidentCreated(IncidentCreatedEvent event) {
        kafkaTemplate.send(TOPIC_CREATED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_CREATED, event.getIncidentId());
    }
    public void sendIncidentUpdated(IncidentUpdatedEvent event) {
        kafkaTemplate.send(TOPIC_UPDATED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_UPDATED, event.getIncidentId());
    }
    public void sendIncidentResolved(IncidentResolvedEvent event) {
        kafkaTemplate.send(TOPIC_RESOLVED, event.getIncidentId().toString(), event);
        log.info("Event sent -> {} : incidentId={}", TOPIC_RESOLVED, event.getIncidentId());
    }
    public void sendSlaBreachEvent(SlaBreachEvent event) {
        kafkaTemplate.send(TOPIC_SLA, event.getIncidentId().toString(), event);
        log.warn("SLA BREACH Event sent -> {} : incidentId={}", TOPIC_SLA, event.getIncidentId());
    }
}


