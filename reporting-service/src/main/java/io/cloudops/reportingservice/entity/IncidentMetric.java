package io.cloudops.reportingservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Projection Oracle des événements Kafka incidents.
 * Utilisée exclusivement pour les agrégations analytiques.
 *
 * NOTE Oracle XE 21c :
 *  - Pas de type BOOLEAN natif → NUMBER(1)  (0=false, 1=true)
 *  - Séquence explicite obligatoire
 *  - Noms de colonnes en MAJUSCULES recommandés
 */
@Entity
@Table(
        name = "INCIDENT_METRICS",
        indexes = {
                @Index(name = "IDX_IM_PRIORITY",   columnList = "PRIORITY"),
                @Index(name = "IDX_IM_STATUS",     columnList = "STATUS"),
                @Index(name = "IDX_IM_CREATED_AT", columnList = "CREATED_AT"),
                @Index(name = "IDX_IM_TEAM_ID",    columnList = "TEAM_ID")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_metric_seq")
    @SequenceGenerator(
            name      = "incident_metric_seq",
            sequenceName = "INCIDENT_METRIC_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    /** ID de l'incident dans incident-service */
    @Column(name = "INCIDENT_ID", nullable = false, unique = true)
    private Long incidentId;

    @Column(name = "TITLE", length = 255)
    private String title;

    /** CRITICAL / HIGH / MEDIUM / LOW */
    @Column(name = "PRIORITY", length = 20)
    private String priority;

    /** OPEN / IN_PROGRESS / RESOLVED / CLOSED / REOPENED */
    @Column(name = "STATUS", length = 20)
    private String status;

    /** Nom du service impacté */
    @Column(name = "SERVICE_IMPACTED", length = 100)
    private String serviceImpacted;

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "TEAM_NAME", length = 100)
    private String teamName;

    @Column(name = "ASSIGNED_TO", length = 100)
    private String assignedTo;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Column(name = "SLA_DEADLINE")
    private LocalDateTime slaDeadline;

    /**
     * Oracle XE 21c ne supporte pas BOOLEAN natif.
     * 0 = SLA respecté, 1 = SLA dépassé
     */
    @Column(name = "SLA_BREACHED", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Integer slaBreached;

    /** Durée de résolution en minutes (calculé à la résolution) */
    @Column(name = "RESOLUTION_TIME_MIN")
    private Long resolutionTimeMinutes;

    // ─── helpers booléens ────────────────────────────────────────────────────

    public boolean isSlaBreached() {
        return Integer.valueOf(1).equals(this.slaBreached);
    }

    public void setSlaBreachedBool(boolean breached) {
        this.slaBreached = breached ? 1 : 0;
    }
}

