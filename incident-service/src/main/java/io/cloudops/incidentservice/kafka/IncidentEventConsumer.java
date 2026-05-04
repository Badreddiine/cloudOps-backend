package io.cloudops.incidentservice.kafka;



import io.cloudops.incidentservice.config.RoutingDataSource;
import io.cloudops.incidentservice.event.IncidentCreatedEvent;
import io.cloudops.incidentservice.event.IncidentResolvedEvent;
import io.cloudops.incidentservice.event.IncidentUpdatedEvent;
import io.cloudops.incidentservice.event.SlaBreachEvent;
import io.cloudops.incidentservice.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventConsumer {

    private final IncidentRepository incidentRepository;

    // ─── incident.created ────────────────────────────────────────────────────

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_CREATED,
            groupId = "incident-replica-af",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentCreatedAF(ConsumerRecord<String, IncidentCreatedEvent> record,
                                    Acknowledgment ack) {
        log.info("[AF] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("af");
            incidentRepository.save(record.value().toEntity());
            ack.acknowledge();
            log.info("[AF] Incident saved successfully : incidentId={}", record.key());
        } catch (Exception e) {
            log.error("[AF] Failed to save incident {}: {}", record.key(), e.getMessage());
            // Do NOT ack → Kafka will redeliver
        } finally {
            RoutingDataSource.clear();
        }
    }

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_CREATED,
            groupId = "incident-replica-us",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentCreatedUS(ConsumerRecord<String, IncidentCreatedEvent> record,
                                    Acknowledgment ack) {
        log.info("[US] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("us");
            incidentRepository.save(record.value().toEntity());
            ack.acknowledge();
            log.info("[US] Incident saved successfully : incidentId={}", record.key());
        } catch (Exception e) {
            log.error("[US] Failed to save incident {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    // ─── incident.updated ────────────────────────────────────────────────────

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_UPDATED,
            groupId = "incident-replica-af",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentUpdatedAF(ConsumerRecord<String, IncidentUpdatedEvent> record,
                                    Acknowledgment ack) {
        log.info("[AF] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("af");
            incidentRepository.updateFromEvent(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[AF] Failed to update incident {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_UPDATED,
            groupId = "incident-replica-us",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentUpdatedUS(ConsumerRecord<String, IncidentUpdatedEvent> record,
                                    Acknowledgment ack) {
        log.info("[US] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("us");
            incidentRepository.updateFromEvent(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[US] Failed to update incident {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    // ─── incident.resolved ───────────────────────────────────────────────────

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_RESOLVED,
            groupId = "incident-replica-af",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentResolvedAF(ConsumerRecord<String, IncidentResolvedEvent> record,
                                     Acknowledgment ack) {
        log.info("[AF] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("af");
            incidentRepository.resolveFromEvent(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[AF] Failed to resolve incident {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_RESOLVED,
            groupId = "incident-replica-us",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onIncidentResolvedUS(ConsumerRecord<String, IncidentResolvedEvent> record,
                                     Acknowledgment ack) {
        log.info("[US] Consuming {} : incidentId={}", record.topic(), record.key());
        try {
            RoutingDataSource.setDataSource("us");
            incidentRepository.resolveFromEvent(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[US] Failed to resolve incident {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    // ─── incident.sla.breach ─────────────────────────────────────────────────

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_SLA,
            groupId = "incident-sla-af",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSlaBreachAF(ConsumerRecord<String, SlaBreachEvent> record,
                              Acknowledgment ack) {
        log.warn("[AF] SLA BREACH received : incidentId={}", record.key());
        try {
            RoutingDataSource.setDataSource("af");
            incidentRepository.markSlaBreached(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[AF] Failed to mark SLA breach {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }

    @KafkaListener(
            topics = IncidentEventProducer.TOPIC_SLA,
            groupId = "incident-sla-us",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSlaBreachUS(ConsumerRecord<String, SlaBreachEvent> record,
                              Acknowledgment ack) {
        log.warn("[US] SLA BREACH received : incidentId={}", record.key());
        try {
            RoutingDataSource.setDataSource("us");
            incidentRepository.markSlaBreached(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[US] Failed to mark SLA breach {}: {}", record.key(), e.getMessage());
        } finally {
            RoutingDataSource.clear();
        }
    }
}
