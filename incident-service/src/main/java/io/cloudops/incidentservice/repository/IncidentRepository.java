package io.cloudops.incidentservice.repository;

import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.event.IncidentResolvedEvent;
import io.cloudops.incidentservice.event.IncidentUpdatedEvent;
import io.cloudops.incidentservice.event.SlaBreachEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long>,
        JpaSpecificationExecutor<Incident> {

    default void updateFromEvent(IncidentUpdatedEvent event) {
        findById(event.getIncidentId()).ifPresent(incident -> {
            incident.setTitle(event.getTitle());
            incident.setStatus(IncidentStatus.valueOf(event.getNewStatus()));
            incident.setLastModifiedByUserId(event.getUpdatedBy());
            save(incident);
        });
    }
    @Query("""
    SELECT i FROM Incident i
    WHERE i.slaBreached = false
      AND i.slaDeadline IS NOT NULL
      AND i.slaDeadline < :now
      AND i.status <> 'RESOLVED'
    """)
    List<Incident> findPendingSlaBreaches(@Param("now") LocalDateTime now);

    default void resolveFromEvent(IncidentResolvedEvent event) {
        findById(event.getIncidentId()).ifPresent(incident -> {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(event.getResolvedAt());
            incident.setLastModifiedByUserId(event.getResolvedBy());
            save(incident);
        });
    }

    default void markSlaBreached(SlaBreachEvent event) {
        findById(event.getIncidentId()).ifPresent(incident -> {
            incident.setSlaBreached(true);
            save(incident);
        });
    }
}