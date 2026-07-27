package io.cloudops.reportingservice.repository;


import io.cloudops.reportingservice.entity.IncidentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository reporting — toutes les requêtes natives PostgreSQL.
 *
 * RÈGLES PostgreSQL appliquées :
 *  - NOW()        au lieu de SYSDATE
 *  - DATE_TRUNC() au lieu de TRUNC()
 *  - INTERVAL     au lieu de ADD_MONTHS()
 *  - COALESCE()   au lieu de NVL()
 *  - SMALLINT (0/1) pour les booléens legacy
 *  - LIMIT / OFFSET pour la pagination (géré par Spring Data)
 */
@Repository
public interface IncidentMetricRepository extends JpaRepository<IncidentMetric, Long> {

    Optional<IncidentMetric> findByIncidentId(Long incidentId);

    boolean existsByIncidentId(Long incidentId);

    // ═══════════════════════════════════════════════════════════════════════
    // KPIs GLOBAUX
    // ═══════════════════════════════════════════════════════════════════════

    @Query("SELECT COUNT(m) FROM IncidentMetric m WHERE m.status IN ('OPEN','IN_PROGRESS')")
    long countOpenIncidents();

    @Query("SELECT COUNT(m) FROM IncidentMetric m WHERE m.status = 'RESOLVED' OR m.status = 'CLOSED'")
    long countResolvedIncidents();

    /** Taux SLA compliance global en % */
    @Query(value = """
        SELECT ROUND(
            (SUM(CASE WHEN sla_breached = 0 THEN 1 ELSE 0 END) * 100.0)
            / NULLIF(COUNT(*), 0), 2
        )
        FROM incident_metrics
        """, nativeQuery = true)
    Double getSlaComplianceRate();

    /** Temps moyen de résolution en minutes */
    @Query(value = """
        SELECT ROUND(AVG(resolution_time_min), 1)
        FROM incident_metrics
        WHERE resolution_time_min IS NOT NULL
        AND status IN ('RESOLVED','CLOSED')
        """, nativeQuery = true)
    Double getAvgResolutionTimeMinutes();

    // ═══════════════════════════════════════════════════════════════════════
    // RÉPARTITION PAR PRIORITÉ
    // ═══════════════════════════════════════════════════════════════════════

    @Query("SELECT m.priority, COUNT(m) FROM IncidentMetric m GROUP BY m.priority ORDER BY COUNT(m) DESC")
    List<Object[]> countByPriority();

    @Query("""
        SELECT m.priority, COUNT(m)
        FROM IncidentMetric m
        WHERE m.createdAt BETWEEN :from AND :to
        GROUP BY m.priority
        """)
    List<Object[]> countByPriorityBetween(
        @Param("from") LocalDateTime from,
        @Param("to")   LocalDateTime to
    );

    // ═══════════════════════════════════════════════════════════════════════
    // RÉPARTITION PAR STATUT
    // ═══════════════════════════════════════════════════════════════════════

    @Query("SELECT m.status, COUNT(m) FROM IncidentMetric m GROUP BY m.status")
    List<Object[]> countByStatus();

    // ═══════════════════════════════════════════════════════════════════════
    // TENDANCES TEMPORELLES — DATE_TRUNC(date) = tronquer au jour
    // ═══════════════════════════════════════════════════════════════════════

    /** Tendance 7 derniers jours — PostgreSQL: NOW() - INTERVAL, DATE_TRUNC() pour tronquer au jour */
    @Query(value = """
        SELECT TO_CHAR(DATE_TRUNC('day', created_at), 'YYYY-MM-DD') AS day_label,
               COUNT(*) AS cnt
        FROM incident_metrics
        WHERE created_at >= NOW() - INTERVAL '7 days'
        GROUP BY DATE_TRUNC('day', created_at)
        ORDER BY DATE_TRUNC('day', created_at)
        """, nativeQuery = true)
    List<Object[]> getLast7DaysTrend();

    /** Tendance 30 derniers jours */
    @Query(value = """
        SELECT TO_CHAR(DATE_TRUNC('day', created_at), 'YYYY-MM-DD') AS day_label,
               COUNT(*) AS cnt
        FROM incident_metrics
        WHERE created_at >= NOW() - INTERVAL '30 days'
        GROUP BY DATE_TRUNC('day', created_at)
        ORDER BY DATE_TRUNC('day', created_at)
        """, nativeQuery = true)
    List<Object[]> getLast30DaysTrend();

    /** Tendance mensuelle 6 derniers mois — PostgreSQL: DATE_TRUNC('month', date) */
    @Query(value = """
        SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS month_label,
               COUNT(*) AS cnt
        FROM incident_metrics
        WHERE created_at >= NOW() - INTERVAL '6 months'
        GROUP BY DATE_TRUNC('month', created_at)
        ORDER BY DATE_TRUNC('month', created_at)
        """, nativeQuery = true)
    List<Object[]> getLast6MonthsTrend();

    // ═══════════════════════════════════════════════════════════════════════
    // PAR ÉQUIPE
    // ═══════════════════════════════════════════════════════════════════════

    @Query("""
        SELECT m.teamName, COUNT(m),
               SUM(CASE WHEN m.slaBreached = 1 THEN 1 ELSE 0 END)
        FROM IncidentMetric m
        WHERE m.teamName IS NOT NULL
        GROUP BY m.teamName
        ORDER BY COUNT(m) DESC
        """)
    List<Object[]> countByTeam();

    @Query("""
        SELECT m.teamName, COUNT(m),
               SUM(CASE WHEN m.slaBreached = 1 THEN 1 ELSE 0 END)
        FROM IncidentMetric m
        WHERE m.teamId = :teamId
          AND m.createdAt BETWEEN :from AND :to
        GROUP BY m.teamName
        """)
    List<Object[]> countByTeamAndPeriod(
        @Param("teamId") Long teamId,
        @Param("from")   LocalDateTime from,
        @Param("to")     LocalDateTime to
    );

    // ═══════════════════════════════════════════════════════════════════════
    // FILTRES COMBINÉS — pour l'API /metrics?dateFrom=&dateTo=&teamId=&service=
    // ═══════════════════════════════════════════════════════════════════════

    @Query("""
        SELECT m FROM IncidentMetric m
        WHERE (:from IS NULL OR m.createdAt >= :from)
          AND (:to IS NULL OR m.createdAt <= :to)
          AND (:teamId IS NULL OR m.teamId = :teamId)
          AND (:service IS NULL OR m.serviceImpacted = :service)
        ORDER BY m.createdAt DESC
        """)
    List<IncidentMetric> findWithFilters(
        @Param("from")    LocalDateTime from,
        @Param("to")      LocalDateTime to,
        @Param("teamId")  Long teamId,
        @Param("service") String service
    );

    @Query("""
        SELECT COUNT(m) FROM IncidentMetric m
        WHERE (:from IS NULL OR m.createdAt >= :from)
          AND (:to IS NULL OR m.createdAt <= :to)
          AND (:teamId IS NULL OR m.teamId = :teamId)
          AND (:service IS NULL OR m.serviceImpacted = :service)
        """)
    long countWithFilters(
        @Param("from")    LocalDateTime from,
        @Param("to")      LocalDateTime to,
        @Param("teamId")  Long teamId,
        @Param("service") String service
    );

    // ═══════════════════════════════════════════════════════════════════════
    // RAPPORT MENSUEL — pour export PDF
    // ═══════════════════════════════════════════════════════════════════════

    /** Incidents d'un mois donné — PostgreSQL: DATE_TRUNC('month', date) */
    @Query(value = """
        SELECT * FROM incident_metrics
        WHERE DATE_TRUNC('month', created_at) = DATE_TRUNC('month', TO_DATE(:monthStart, 'YYYY-MM-DD'))
        ORDER BY created_at
        """, nativeQuery = true)
    List<IncidentMetric> findByMonth(@Param("monthStart") String monthStart);

    /** Stats agrégées pour rapport mensuel */
    @Query(value = """
        SELECT
            COUNT(*) AS total,
            SUM(CASE WHEN priority = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_cnt,
            SUM(CASE WHEN priority = 'HIGH'     THEN 1 ELSE 0 END) AS high_cnt,
            SUM(CASE WHEN priority = 'MEDIUM'   THEN 1 ELSE 0 END) AS medium_cnt,
            SUM(CASE WHEN priority = 'LOW'      THEN 1 ELSE 0 END) AS low_cnt,
            SUM(CASE WHEN status IN ('RESOLVED','CLOSED') THEN 1 ELSE 0 END) AS resolved_cnt,
            SUM(CASE WHEN sla_breached = 1 THEN 1 ELSE 0 END) AS breached_cnt,
            ROUND(AVG(resolution_time_min), 1) AS avg_resolution_min
        FROM incident_metrics
        WHERE DATE_TRUNC('month', created_at) = DATE_TRUNC('month', TO_DATE(:monthStart, 'YYYY-MM-DD'))
        """, nativeQuery = true)
    Object[] getMonthlyStats(@Param("monthStart") String monthStart);

    // ═══════════════════════════════════════════════════════════════════════
    // MISE À JOUR PARTIELLE (évite de refetch l'objet entier)
    // ═══════════════════════════════════════════════════════════════════════

    @Modifying
    @Transactional
    @Query("""
        UPDATE IncidentMetric m
        SET m.status = :status, m.updatedAt = :updatedAt
        WHERE m.incidentId = :incidentId
        """)
    int updateStatus(
        @Param("incidentId") Long incidentId,
        @Param("status")     String status,
        @Param("updatedAt")  LocalDateTime updatedAt
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE IncidentMetric m
        SET m.status = :status,
            m.resolvedAt = :resolvedAt,
            m.resolutionTimeMinutes = :resolutionMin,
            m.updatedAt = :updatedAt
        WHERE m.incidentId = :incidentId
        """)
    int updateResolution(
        @Param("incidentId")    Long incidentId,
        @Param("status")        String status,
        @Param("resolvedAt")    LocalDateTime resolvedAt,
        @Param("resolutionMin") Long resolutionMin,
        @Param("updatedAt")     LocalDateTime updatedAt
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE IncidentMetric m
        SET m.slaBreached = 1, m.updatedAt = :now
        WHERE m.incidentId = :incidentId
        """)
    int markSlaBreached(
        @Param("incidentId") Long incidentId,
        @Param("now")        LocalDateTime now
    );
}
