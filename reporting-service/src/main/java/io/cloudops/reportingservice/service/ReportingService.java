package io.cloudops.reportingservice.service;


import io.cloudops.reportingservice.dto.*;
import io.cloudops.reportingservice.entity.IncidentMetric;
import io.cloudops.reportingservice.repository.IncidentMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportingService {

    private final IncidentMetricRepository repository;

    // ── Dashboard complet ─────────────────────────────────────────────────

    @Cacheable(value = "dashboard", key = "'global'")
    public DashboardResponseDTO getDashboard() {
        log.debug("[Reporting] Cache MISS → calcul dashboard depuis Oracle");
        return DashboardResponseDTO.builder()
            .kpis(buildKpis())
            .byPriority(buildByPriority(null, null))
            .byStatus(buildByStatus())
            .trend7Days(buildTrend(7))
            .trend30Days(buildTrend(30))
            .byTeam(buildByTeam())
            .generatedAt(LocalDateTime.now())
            .build();
    }

    // ── KPIs ──────────────────────────────────────────────────────────────

    @Cacheable(value = "sla-stats", key = "'global'")
    public KpiDTO getKpis() {
        return buildKpis();
    }

    private KpiDTO buildKpis() {
        return KpiDTO.builder()
            .totalOpen(repository.countOpenIncidents())
            .totalResolved(repository.countResolvedIncidents())
            .slaComplianceRate(nvl(repository.getSlaComplianceRate()))
            .avgResolutionTimeMinutes(nvl(repository.getAvgResolutionTimeMinutes()))
            .build();
    }

    // ── Répartitions ──────────────────────────────────────────────────────

    @Cacheable(value = "metrics", key = "'priority_' + #from + '_' + #to")
    public List<ChartPointDTO> getPriorityChart(String from, String to) {
        return buildByPriority(from, to);
    }

    private List<ChartPointDTO> buildByPriority(String from, String to) {
        List<Object[]> rows = (from != null && to != null)
            ? repository.countByPriorityBetween(parseDate(from), parseDate(to))
            : repository.countByPriority();
        return toChartPoints(rows);
    }

    private List<ChartPointDTO> buildByStatus() {
        return toChartPoints(repository.countByStatus());
    }

    private List<ChartPointDTO> toChartPoints(List<Object[]> rows) {
        return rows.stream()
            .map(r -> ChartPointDTO.builder().label(str(r[0])).value(num(r[1])).build())
            .collect(Collectors.toList());
    }

    // ── Tendances ─────────────────────────────────────────────────────────

    @Cacheable(value = "metrics", key = "'trend7'")
    public List<TrendPointDTO> getTrend7Days() {
        return buildTrend(7);
    }

    @Cacheable(value = "metrics", key = "'trend30'")
    public List<TrendPointDTO> getTrend30Days() {
        return buildTrend(30);
    }

    @Cacheable(value = "metrics", key = "'trend6months'")
    public List<TrendPointDTO> getTrend6Months() {
        return repository.getLast6MonthsTrend().stream()
            .map(r -> TrendPointDTO.builder().date(str(r[0])).count(num(r[1])).build())
            .collect(Collectors.toList());
    }

    private List<TrendPointDTO> buildTrend(int days) {
        List<Object[]> rows = (days == 7)
            ? repository.getLast7DaysTrend()
            : repository.getLast30DaysTrend();
        return rows.stream()
            .map(r -> TrendPointDTO.builder().date(str(r[0])).count(num(r[1])).build())
            .collect(Collectors.toList());
    }

    // ── Stats par équipe ──────────────────────────────────────────────────

    @Cacheable(value = "team-stats", key = "'all'")
    public List<TeamStatDTO> getTeamStats() {
        return buildByTeam();
    }

    private List<TeamStatDTO> buildByTeam() {
        return repository.countByTeam().stream()
            .map(r -> {
                long total    = num(r[1]);
                long breached = num(r[2]);
                double comp   = total > 0
                    ? Math.round(((double)(total - breached) / total) * 10000.0) / 100.0
                    : 100.0;
                return TeamStatDTO.builder()
                    .teamName(str(r[0]))
                    .total(total)
                    .slaBreached(breached)
                    .slaCompliancePercent(comp)
                    .build();
            })
            .collect(Collectors.toList());
    }

    // ── Incidents filtrés ─────────────────────────────────────────────────

    @Cacheable(value = "metrics",
        key = "'filtered_' + #from + '_' + #to + '_' + #teamId + '_' + #service")
    public List<ReportRowDTO> getFilteredIncidents(
            String from, String to, Long teamId, String service) {
        LocalDateTime dtFrom = from != null ? parseDate(from)              : null;
        LocalDateTime dtTo   = to   != null ? parseDate(to).plusDays(1)    : null;
        return repository.findWithFilters(dtFrom, dtTo, teamId, service)
            .stream().map(this::toRow).collect(Collectors.toList());
    }

    // ── Rapport mensuel ───────────────────────────────────────────────────

    @Cacheable(value = "metrics", key = "'monthly_' + #month")
    public MonthlyReportDTO getMonthlyReport(String month) {
        String monthStart = month + "-01";
        Object[] s        = repository.getMonthlyStats(monthStart);
        List<IncidentMetric> incidents = repository.findByMonth(monthStart);

        long total    = numObj(s[0]);
        long breached = numObj(s[6]);
        double comp   = total > 0
            ? Math.round(((double)(total - breached) / total) * 10000.0) / 100.0
            : 100.0;

        return MonthlyReportDTO.builder()
            .month(month)
            .total(total)
            .criticalCount(numObj(s[1]))
            .highCount(numObj(s[2]))
            .mediumCount(numObj(s[3]))
            .lowCount(numObj(s[4]))
            .resolvedCount(numObj(s[5]))
            .slaBreachedCount(breached)
            .avgResolutionMinutes(s[7] != null ? ((Number)s[7]).doubleValue() : 0.0)
            .slaComplianceRate(comp)
            .incidents(incidents.stream().map(this::toRow).collect(Collectors.toList()))
            .build();
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private ReportRowDTO toRow(IncidentMetric m) {
        return ReportRowDTO.builder()
            .incidentId(m.getIncidentId())
            .title(m.getTitle())
            .priority(m.getPriority())
            .status(m.getStatus())
            .serviceImpacted(m.getServiceImpacted())
            .teamName(m.getTeamName())
            .assignedTo(m.getAssignedTo())
            .createdAt(m.getCreatedAt())
            .resolvedAt(m.getResolvedAt())
            .resolutionTimeMinutes(m.getResolutionTimeMinutes())
            .slaBreached(m.isSlaBreached())
            .build();
    }

    // ── Utils ─────────────────────────────────────────────────────────────

    private LocalDateTime parseDate(String d) {
        return LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    private String str(Object o)  { return o != null ? o.toString() : ""; }
    private long   num(Object o)  { return o == null ? 0L : ((Number)o).longValue(); }
    private long   numObj(Object o) { return num(o); }
    private double nvl(Double d)  { return d != null ? d : 0.0; }
}
