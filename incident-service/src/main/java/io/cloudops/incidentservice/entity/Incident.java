package io.cloudops.incidentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Incident {

    // ── AVANT (PostgreSQL) : BIGSERIAL → GenerationType.IDENTITY
    // ── APRÈS (Oracle)     : NUMBER(19) GENERATED ALWAYS AS IDENTITY → identique
    //    → Aucun changement nécessaire sur l'ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // ── AVANT (PostgreSQL) : TEXT → pas d'annotation @Lob nécessaire
    // ── APRÈS (Oracle)     : CLOB → @Lob obligatoire pour les champs > 255 chars
    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    private IncidentCategory category;

    private String assignedTo;          // username Keycloak
    private String reportedBy;         // username Keycloak (extrait du JWT)

    private LocalDateTime slaDeadline; // calculé par SlaStrategy
    private LocalDateTime resolvedAt;

    // ── AVANT (PostgreSQL) : BOOLEAN DEFAULT FALSE → Boolean Java
    // ── APRÈS (Oracle)     : NUMBER(1) DEFAULT 0  → Boolean Java
    //    → Hibernate mappe automatiquement Boolean ↔ NUMBER(1) avec OracleDialect
    //    → Aucun changement sur le champ Java, le dialecte Oracle gère la conversion
    @Column(name = "sla_breached", nullable = false)
    @Builder.Default
    private Boolean slaBreached = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    private String lastModifiedByUserId;

    public void addAuditLog(AuditLog auditLog) {
        auditLogs.add(auditLog);
        auditLog.setIncident(this);
    }
}