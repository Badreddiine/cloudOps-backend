package io.cloudops.incidentservice.service;
// WRONG IMPORT FOR THE PRODUCER

import io.cloudops.incidentservice.dto.request.CreateIncidentRequest;
import io.cloudops.incidentservice.dto.request.UpdateIncidentRequest;
import io.cloudops.incidentservice.dto.response.IncidentResponse;
import io.cloudops.incidentservice.entity.AuditLog;
import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.exception.IncidentNotFoundException;
import io.cloudops.incidentservice.factory.IncidentFactory;
import io.cloudops.incidentservice.kafka.IncidentEventProducer;
import io.cloudops.incidentservice.mapper.IncidentMapper;
import io.cloudops.incidentservice.repository.AuditLogRepository;
import io.cloudops.incidentservice.repository.IncidentRepository;
import io.cloudops.incidentservice.statemachine.IncidentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// CORRECT IMPORTS
import io.cloudops.event.IncidentCreatedEvent;
import io.cloudops.event.IncidentResolvedEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository    incidentRepository;
    private final AuditLogRepository    auditLogRepository;
    private final IncidentMapper        incidentMapper;
    private final IncidentFactory       incidentFactory;
    private final IncidentStateMachine  incidentStateMachine;
    private final IncidentEventProducer eventProducer;          // ← ajout

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request, String reportedBy) {
        Incident incident = incidentFactory.create(request, reportedBy);
        incident = incidentRepository.save(incident);

        AuditLog auditLog = AuditLog.builder()
                .incident(incident)
                .action("CREATE")
                .newValue(incident.getStatus().name())
                .performedBy(reportedBy)
                .timestamp(LocalDateTime.now())
                .build();
        incident.addAuditLog(auditLog);

        // Publication Kafka APRÈS le save (DB = source de vérité)
        eventProducer.sendIncidentCreated(incidentMapper.toCreatedEvent(incident));

        log.info("[Service] Incident created & event sent: id={}", incident.getId());
        return incidentMapper.toIncidentResponse(incident);
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public IncidentResponse findIncidentById(Long id) {
        return incidentRepository.findById(id)
                .map(incidentMapper::toIncidentResponse)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> findAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(incidentMapper::toIncidentResponse)
                .collect(Collectors.toList());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public IncidentResponse updateIncident(Long id, UpdateIncidentRequest request, String lastModifiedBy) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));

        String oldStatus   = existingIncident.getStatus().name();
        String oldPriority = existingIncident.getPriority().name();

        // Champs simples
        Optional.ofNullable(request.getTitle())      .ifPresent(existingIncident::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(existingIncident::setDescription);
        Optional.ofNullable(request.getCategory())   .ifPresent(existingIncident::setCategory);
        Optional.ofNullable(request.getAssignedTo()) .ifPresent(existingIncident::setAssignedTo);

        // Changement de priorité → recalcul SLA
        if (request.getPriority() != null && !request.getPriority().equals(existingIncident.getPriority())) {
            existingIncident.setPriority(request.getPriority());
            existingIncident.setSlaDeadline(
                    incidentFactory.getSlaStrategyFactory()
                            .getStrategy(request.getPriority())
                            .calculateDeadline(LocalDateTime.now())
            );
        }

        // Changement de statut → state machine + resolvedAt
        if (request.getStatus() != null && !request.getStatus().equals(existingIncident.getStatus())) {
            incidentStateMachine.validateTransition(existingIncident.getStatus(), request.getStatus());
            existingIncident.setStatus(request.getStatus());

            if (request.getStatus() == IncidentStatus.RESOLVED) {
                existingIncident.setResolvedAt(LocalDateTime.now());
            } else if (request.getStatus() == IncidentStatus.OPEN ||
                    request.getStatus() == IncidentStatus.REOPENED) {
                existingIncident.setResolvedAt(null);
            }
        }

        existingIncident.setUpdatedAt(LocalDateTime.now());
        existingIncident.setLastModifiedByUserId(lastModifiedBy);

        Incident updatedIncident = incidentRepository.save(existingIncident);

        // Audit + événements Kafka APRÈS le save
        boolean statusChanged   = !oldStatus.equals(updatedIncident.getStatus().name());
        boolean priorityChanged = !oldPriority.equals(updatedIncident.getPriority().name());

        if (statusChanged) {
            updatedIncident.addAuditLog(AuditLog.builder()
                    .incident(updatedIncident)
                    .action("STATUS_CHANGE")
                    .oldValue(oldStatus)
                    .newValue(updatedIncident.getStatus().name())
                    .performedBy(lastModifiedBy)
                    .timestamp(LocalDateTime.now())
                    .build());

            // Kafka : updated ou resolved selon le nouveau statut
            if (updatedIncident.getStatus() == IncidentStatus.RESOLVED) {
                eventProducer.sendIncidentResolved(incidentMapper.toResolvedEvent(updatedIncident));
            } else {
                eventProducer.sendIncidentUpdated(incidentMapper.toUpdatedEvent(updatedIncident, oldStatus));
            }
        }

        if (priorityChanged) {
            updatedIncident.addAuditLog(AuditLog.builder()
                    .incident(updatedIncident)
                    .action("PRIORITY_CHANGE")
                    .oldValue(oldPriority)
                    .newValue(updatedIncident.getPriority().name())
                    .performedBy(lastModifiedBy)
                    .timestamp(LocalDateTime.now())
                    .build());

            // Kafka : updated pour signaler le changement de priorité
            if (!statusChanged) { // éviter un double envoi si les deux changent ensemble
                eventProducer.sendIncidentUpdated(incidentMapper.toUpdatedEvent(updatedIncident, oldStatus));
            }
        }

        log.info("[Service] Incident updated: id={} status={} priority={}",
                updatedIncident.getId(), updatedIncident.getStatus(), updatedIncident.getPriority());
        return incidentMapper.toIncidentResponse(updatedIncident);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        incidentRepository.delete(existingIncident);
        log.info("[Service] Incident deleted: id={}", id);
    }

    // ─── SLA ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void markSlaBreached(Long incidentId) {
        incidentRepository.findById(incidentId).ifPresent(incident -> {
            if (!incident.getSlaBreached()) {
                incident.setSlaBreached(true);
                incidentRepository.save(incident);
                // L'événement SLA est publié par le SlaBreachScheduler (séparation des responsabilités)
                log.warn("[Service] SLA marked as breached for incidentId={}", incidentId);
            }
        });
    }
}