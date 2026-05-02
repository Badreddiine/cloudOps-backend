package io.cloudops.incidentservice.service;

import io.cloudops.incidentservice.dto.request.CreateIncidentRequest;
import io.cloudops.incidentservice.dto.request.UpdateIncidentRequest;
import io.cloudops.incidentservice.dto.response.IncidentResponse;
import io.cloudops.incidentservice.entity.AuditLog;
import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.exception.IncidentNotFoundException;
import io.cloudops.incidentservice.factory.IncidentFactory;
import io.cloudops.incidentservice.mapper.IncidentMapper;
import io.cloudops.incidentservice.repository.AuditLogRepository;
import io.cloudops.incidentservice.repository.IncidentRepository;
import io.cloudops.incidentservice.statemachine.IncidentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;
    private final IncidentMapper incidentMapper;
    private final IncidentFactory incidentFactory;
    private final IncidentStateMachine incidentStateMachine;

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

        log.info("Incident created: {}", incident.getId());
        return incidentMapper.toIncidentResponse(incident);
    }

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

    @Override
    @Transactional
    public IncidentResponse updateIncident(Long id, UpdateIncidentRequest request, String lastModifiedBy) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));

        String oldStatus = existingIncident.getStatus().name();
        String oldPriority = existingIncident.getPriority().name();

        Optional.ofNullable(request.getTitle()).ifPresent(existingIncident::setTitle);
        Optional.ofNullable(request.getDescription()).ifPresent(existingIncident::setDescription);
        Optional.ofNullable(request.getCategory()).ifPresent(existingIncident::setCategory);
        Optional.ofNullable(request.getAssignedTo()).ifPresent(existingIncident::setAssignedTo);

        if (request.getPriority() != null && !request.getPriority().equals(existingIncident.getPriority())) {
            existingIncident.setPriority(request.getPriority());
            // ✅ Ligne corrigée (était tronquée)
            existingIncident.setSlaDeadline(
                    incidentFactory.getSlaStrategyFactory()
                            .getStrategy(request.getPriority())
                            .calculateDeadline(LocalDateTime.now())
            );
        }

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

        if (!oldStatus.equals(updatedIncident.getStatus().name())) {
            AuditLog statusChangeLog = AuditLog.builder()
                    .incident(updatedIncident)
                    .action("STATUS_CHANGE")
                    .oldValue(oldStatus)
                    .newValue(updatedIncident.getStatus().name())
                    .performedBy(lastModifiedBy)
                    .timestamp(LocalDateTime.now())
                    .build();
            updatedIncident.addAuditLog(statusChangeLog);
        }

        if (!oldPriority.equals(updatedIncident.getPriority().name())) {
            AuditLog priorityChangeLog = AuditLog.builder()
                    .incident(updatedIncident)
                    .action("PRIORITY_CHANGE")
                    .oldValue(oldPriority)
                    .newValue(updatedIncident.getPriority().name())
                    .performedBy(lastModifiedBy)
                    .timestamp(LocalDateTime.now())
                    .build();
            updatedIncident.addAuditLog(priorityChangeLog);
        }

        log.info("Incident updated: {}", updatedIncident.getId());
        return incidentMapper.toIncidentResponse(updatedIncident);
    }

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with ID: " + id));
        incidentRepository.delete(existingIncident);
        log.info("Incident deleted: {}", id);
    }

    @Override
    @Transactional
    public void markSlaBreached(Long incidentId) {
        incidentRepository.findById(incidentId).ifPresent(incident -> {
            if (!incident.getSlaBreached()) {
                incident.setSlaBreached(true);
                incidentRepository.save(incident);
                log.warn("SLA breached for incident: {}", incidentId);
                // TODO: Publier l'événement SLA breach sur Kafka
            }
        });
    }
}