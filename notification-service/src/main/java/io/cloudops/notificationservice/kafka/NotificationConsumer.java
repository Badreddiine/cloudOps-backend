package io.cloudops.notificationservice.kafka;

import io.cloudops.notificationservice.service.NotificationService;
import io.cloudops.event.IncidentCreatedEvent;
import io.cloudops.event.IncidentResolvedEvent;
import io.cloudops.event.IncidentUpdatedEvent;
import io.cloudops.event.SlaBreachEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "incident.created", groupId = "notification-service")
    public void onIncidentCreated(IncidentCreatedEvent event) {
        log.info("[NOTIF] incident.created : {}", event.getIncidentId());

        // WebSocket → reporter
        notificationService.sendWebSocket(
                event.getReportedBy(),
                "INCIDENT_CREATED",
                "Incident créé : " + event.getTitle(),
                event.getIncidentId()
        );

        // Email
        Context ctx = new Context();
        ctx.setVariable("title", event.getTitle());
        ctx.setVariable("incidentId", event.getIncidentId());
        ctx.setVariable("reportedBy", event.getReportedBy());
        notificationService.sendEmail(
                event.getReportedBy() + "@cloudops.io",
                "[CloudOps] Incident créé : " + event.getTitle(),
                "incident-created",
                ctx
        );
    }

    @KafkaListener(topics = "incident.updated", groupId = "notification-service")
    public void onIncidentUpdated(IncidentUpdatedEvent event) {
        log.info("[NOTIF] incident.updated : {}", event.getIncidentId());
        notificationService.sendWebSocket(
                event.getUpdatedBy(),
                "INCIDENT_UPDATED",
                "Incident mis à jour : " + event.getTitle()
                        + " → " + event.getNewStatus(),
                event.getIncidentId()
        );
    }

    @KafkaListener(topics = "incident.resolved", groupId = "notification-service")
    public void onIncidentResolved(IncidentResolvedEvent event) {
        log.info("[NOTIF] incident.resolved : {}", event.getIncidentId());

        Context ctx = new Context();
        ctx.setVariable("title", event.getTitle());
        ctx.setVariable("resolvedBy", event.getResolvedBy());
        ctx.setVariable("resolvedAt", event.getResolvedAt());
        notificationService.sendEmail(
                event.getResolvedBy() + "@cloudops.io",
                "[CloudOps] Incident résolu : " + event.getTitle(),
                "incident-resolved",
                ctx
        );
    }

    @KafkaListener(topics = "incident.sla.breach", groupId = "notification-service")
    public void onSlaBreach(SlaBreachEvent event) {
        log.warn("[NOTIF] SLA BREACH : {}", event.getIncidentId());

        // WebSocket broadcast à tous les admins
        notificationService.sendWebSocket(
                "admin",
                "SLA_BREACH",
                "SLA expiré ! Incident : " + event.getTitle(),
                event.getIncidentId()
        );

        // Email d'alerte
        Context ctx = new Context();
        ctx.setVariable("title", event.getTitle());
        ctx.setVariable("slaDeadline", event.getSlaDeadline());
        ctx.setVariable("breachedAt", event.getBreachedAt());
        notificationService.sendEmail(
                "admin@cloudops.io",
                "[ALERTE SLA] Breach détecté : " + event.getTitle(),
                "sla-breach",
                ctx
        );
    }
}
