package io.cloudops.reportingservice.event;


import io.cloudops.reportingservice.entity.IncidentMetric;
import io.cloudops.reportingservice.repository.IncidentMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Consomme les événements Kafka produits par l'incident-service.
 * Met à jour la table INCIDENT_METRICS (projection read-side CQRS).
 * Invalide le cache Redis à chaque modification.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventConsumer {

    private final IncidentMetricRepository repository;

    // ── Topics attendus (doivent correspondre à ceux de l'incident-service) ─

    @KafkaListener(
        topics   = "${cloudops.kafka.topics.incident-created:incident-created}",
        groupId  = "${spring.kafka.consumer.group-id:reporting-service}"
    )
    @CacheEvict(value = {"dashboard", "metrics", "sla-stats", "team-stats"}, allEntries = true)
    public void onIncidentCreated(
        @Payload Map<String, Object> event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            log.info("[Reporting] Received incident-created event: {}", event.get("incidentId"));

            Long incidentId = toLong(event.get("incidentId"));
            if (incidentId == null) {
                log.warn("[Reporting] incident-created event sans incidentId, ignoré");
                return;
            }

            // Idempotence : évite les doublons si Kafka re-livre
            if (repository.existsByIncidentId(incidentId)) {
                log.debug("[Reporting] IncidentMetric {} déjà existant, skip", incidentId);
                return;
            }

            IncidentMetric metric = IncidentMetric.builder()
                .incidentId(incidentId)
                .title(toString(event.get("title")))
                .priority(toString(event.get("priority")))
                .status("OPEN")
                .serviceImpacted(toString(event.get("serviceImpacted")))
                .teamId(toLong(event.get("teamId")))
                .teamName(toString(event.get("teamName")))
                .assignedTo(toString(event.get("assignedTo")))
                .createdAt(toDateTime(event.get("createdAt")))
                .updatedAt(LocalDateTime.now())
                .slaDeadline(toDateTime(event.get("slaDeadline")))
                .slaBreached(0)
                .build();

            repository.save(metric);
            log.info("[Reporting] IncidentMetric créée pour incidentId={}", incidentId);

        } catch (Exception e) {
            log.error("[Reporting] Erreur traitement incident-created: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics  = "${cloudops.kafka.topics.incident-updated:incident-updated}",
        groupId = "${spring.kafka.consumer.group-id:reporting-service}"
    )
    @CacheEvict(value = {"dashboard", "metrics", "sla-stats"}, allEntries = true)
    public void onIncidentUpdated(
        @Payload Map<String, Object> event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            Long incidentId = toLong(event.get("incidentId"));
            String newStatus = toString(event.get("status"));

            if (incidentId == null || newStatus == null) return;

            int updated = repository.updateStatus(incidentId, newStatus, LocalDateTime.now());
            log.info("[Reporting] Status mis à jour incidentId={} → {} ({}row)", incidentId, newStatus, updated);

        } catch (Exception e) {
            log.error("[Reporting] Erreur traitement incident-updated: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics  = "${cloudops.kafka.topics.incident-resolved:incident-resolved}",
        groupId = "${spring.kafka.consumer.group-id:reporting-service}"
    )
    @CacheEvict(value = {"dashboard", "metrics", "sla-stats", "team-stats"}, allEntries = true)
    public void onIncidentResolved(
        @Payload Map<String, Object> event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            Long incidentId   = toLong(event.get("incidentId"));
            LocalDateTime resolvedAt = LocalDateTime.now();

            if (incidentId == null) return;

            // Récupère l'heure de création pour calculer la durée de résolution
            repository.findByIncidentId(incidentId).ifPresent(metric -> {
                long resolutionMin = ChronoUnit.MINUTES.between(
                    metric.getCreatedAt(), resolvedAt
                );
                repository.updateResolution(
                    incidentId, "RESOLVED", resolvedAt, resolutionMin, resolvedAt
                );
                log.info("[Reporting] Résolution enregistrée incidentId={} en {} min", incidentId, resolutionMin);
            });

        } catch (Exception e) {
            log.error("[Reporting] Erreur traitement incident-resolved: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics  = "${cloudops.kafka.topics.sla-breach:sla-breach-event}",
        groupId = "${spring.kafka.consumer.group-id:reporting-service}"
    )
    @CacheEvict(value = {"dashboard", "sla-stats"}, allEntries = true)
    public void onSlaBreached(
        @Payload Map<String, Object> event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            Long incidentId = toLong(event.get("incidentId"));
            if (incidentId == null) return;

            repository.markSlaBreached(incidentId, LocalDateTime.now());
            log.info("[Reporting] SLA breach enregistré pour incidentId={}", incidentId);

        } catch (Exception e) {
            log.error("[Reporting] Erreur traitement sla-breach: {}", e.getMessage(), e);
        }
    }

    // ── Helpers de conversion (les events Kafka arrivent comme Map<String,Object>) ─

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        try { return Long.parseLong(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private String toString(Object val) {
        return val != null ? val.toString() : null;
    }

    private LocalDateTime toDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDateTime ldt) return ldt;
        try { return LocalDateTime.parse(val.toString()); }
        catch (Exception e) { return null; }
    }
}
