package io.cloudops.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    private String action;           // CREATE, UPDATE, STATUS_CHANGE, ASSIGN
    private String oldValue;
    private String newValue;
    private String performedBy;      // userId Keycloak (extrait du JWT via AOP)

    @CreationTimestamp
    private LocalDateTime timestamp;
}
