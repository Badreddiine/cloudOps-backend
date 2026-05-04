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

    // ── AVANT (PostgreSQL) : @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ── APRÈS (Oracle)     : identique — NUMBER(19) GENERATED ALWAYS AS IDENTITY
    //    → GenerationType.IDENTITY est le bon mapping Oracle, rien à changer ici
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    private String action;          // CREATE, UPDATE, STATUS_CHANGE, ASSIGN

    // ── AVANT (PostgreSQL) : TEXT → String sans annotation
    // ── APRÈS (Oracle)     : CLOB → @Lob obligatoire sinon Hibernate tronque à 255 chars
    @Lob
    @Column(name = "old_value")
    private String oldValue;

    @Lob
    @Column(name = "new_value")
    private String newValue;

    private String performedBy;     // userId Keycloak (extrait du JWT via AOP)

    @CreationTimestamp
    private LocalDateTime timestamp;
}