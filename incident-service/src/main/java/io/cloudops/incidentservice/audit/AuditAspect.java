package io.cloudops.incidentservice.audit;

import io.cloudops.incidentservice.dto.response.IncidentResponse;
import io.cloudops.incidentservice.entity.AuditLog;
import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.repository.AuditLogRepository;
import io.cloudops.incidentservice.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final IncidentRepository incidentRepository;

    // Pointcut factorisé
    @Pointcut("execution(* io.cloudops.incidentservice.service.impl.IncidentServiceImpl.createIncident(..)) || " +
            "execution(* io.cloudops.incidentservice.service.impl.IncidentServiceImpl.updateIncident(..))")
    public void incidentOperations() {}

    @AfterReturning(pointcut = "incidentOperations()", returning = "result")
    public void logIncidentAction(JoinPoint joinPoint, Object result) {

        if (!(result instanceof IncidentResponse response)) {
            return;
        }

        Incident incident = incidentRepository
                .findById(response.getId())
                .orElse(null);

        if (incident == null) {
            log.warn("Incident not found for audit logging: {}", response.getId());
            return;
        }

        String userId = extractUserIdFromContext();
        String methodName = joinPoint.getSignature().getName();

        AuditLog auditLog = AuditLog.builder()
                .incident(incident)
                .action(methodName.toUpperCase())
                .newValue(response.getStatus().name())
                .performedBy(userId)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        log.info("Audit log created for incident {}: Action {}",
                response.getId(), methodName);
    }

    private String extractUserIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject(); // Keycloak userId
        }

        return "system";
    }
}