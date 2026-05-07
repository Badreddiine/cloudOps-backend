package io.cloudops.reportingservice.controller;

import io.cloudops.reportingservice.dto.ChartPointDTO;
import io.cloudops.reportingservice.dto.DashboardResponseDTO;
import io.cloudops.reportingservice.dto.KpiDTO;
import io.cloudops.reportingservice.dto.MonthlyReportDTO;
import io.cloudops.reportingservice.dto.ReportRowDTO;
import io.cloudops.reportingservice.dto.TeamStatDTO;
import io.cloudops.reportingservice.dto.TrendPointDTO;
import io.cloudops.reportingservice.export.CsvExportService;
import io.cloudops.reportingservice.export.ExcelExportService;
import io.cloudops.reportingservice.export.PdfExportService;
import io.cloudops.reportingservice.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reporting", description = "Dashboard analytics et exports")
@SecurityRequirement(name = "bearerAuth")
public class ReportingController {

    private final ReportingService reportingService;
    private final CsvExportService csvService;
    private final ExcelExportService excelService;
    private final PdfExportService pdfService;

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS','ROLE_SUPPORT')")
    @Operation(summary = "Dashboard complet",
            description = "KPIs + graphiques + tendances - mis en cache Redis 5 min")
    public ResponseEntity<DashboardResponseDTO> getDashboard() {
        log.debug("[Reporting] GET /dashboard");
        return ResponseEntity.ok(reportingService.getDashboard());
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS','ROLE_SUPPORT')")
    @Operation(summary = "KPIs uniquement")
    public ResponseEntity<KpiDTO> getKpis() {
        return ResponseEntity.ok(reportingService.getKpis());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // METRIQUES FILTREES
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Incidents filtres pour affichage ou export")
    public ResponseEntity<List<ReportRowDTO>> getMetrics(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long   teamId,
            @RequestParam(required = false) String service
    ) {
        log.debug("[Reporting] GET /metrics dateFrom={} dateTo={} teamId={} service={}",
                dateFrom, dateTo, teamId, service);
        return ResponseEntity.ok(
                reportingService.getFilteredIncidents(dateFrom, dateTo, teamId, service)
        );
    }

    @GetMapping("/metrics/teams")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Stats incidents par equipe")
    public ResponseEntity<List<TeamStatDTO>> getTeamStats() {
        return ResponseEntity.ok(reportingService.getTeamStats());
    }

    @GetMapping("/metrics/trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS','ROLE_SUPPORT')")
    @Operation(summary = "Tendance mensuelle 6 mois")
    public ResponseEntity<List<TrendPointDTO>> getTrend6Months() {
        return ResponseEntity.ok(reportingService.getTrend6Months());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPORTS
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Export CSV des incidents filtres")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long   teamId,
            @RequestParam(required = false) String service
    ) {
        log.info("[Reporting] Export CSV demande");
        List<ReportRowDTO> incidents =
                reportingService.getFilteredIncidents(dateFrom, dateTo, teamId, service);
        byte[] data = csvService.exportIncidents(incidents);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"incidents.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(data.length)
                .body(data);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Export Excel XLSX des incidents filtres")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long   teamId,
            @RequestParam(required = false) String service
    ) throws IOException {
        log.info("[Reporting] Export Excel demande");
        List<ReportRowDTO> incidents =
                reportingService.getFilteredIncidents(dateFrom, dateTo, teamId, service);

        String title = "CloudOps Incidents"
                + (dateFrom != null ? " du " + dateFrom : "")
                + (dateTo   != null ? " au " + dateTo   : "");

        byte[] data = excelService.exportIncidents(incidents, title);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cloudops-incidents.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }

    @GetMapping("/export/pdf/{month}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Rapport PDF mensuel (ADMIN uniquement)")
    public ResponseEntity<byte[]> exportMonthlyPdf(
            @PathVariable String month
    ) throws IOException {
        log.info("[Reporting] Export PDF mensuel demande : {}", month);
        MonthlyReportDTO report = reportingService.getMonthlyReport(month);
        byte[] data = pdfService.generateMonthlyReport(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"rapport-cloudops-" + month + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(data);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Export PDF des incidents filtres")
    public ResponseEntity<byte[]> exportFilteredPdf(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long   teamId,
            @RequestParam(required = false) String service
    ) throws IOException {
        log.info("[Reporting] Export PDF filtre demande");
        List<ReportRowDTO> incidents =
                reportingService.getFilteredIncidents(dateFrom, dateTo, teamId, service);
        String description = buildFilterDescription(dateFrom, dateTo, teamId, service);
        byte[] data = pdfService.generateFilteredReport(incidents, description);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cloudops-incidents.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RAPPORT MENSUEL JSON
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/monthly/{month}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DEVOPS')")
    @Operation(summary = "Donnees rapport mensuel (JSON)")
    public ResponseEntity<MonthlyReportDTO> getMonthlyReport(@PathVariable String month) {
        return ResponseEntity.ok(reportingService.getMonthlyReport(month));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════════════════

    private String buildFilterDescription(
            String from, String to, Long teamId, String service) {
        StringBuilder sb = new StringBuilder();
        if (from    != null) sb.append("Du ").append(from);
        if (to      != null) sb.append(" au ").append(to);
        if (teamId  != null) sb.append(" | Equipe ").append(teamId);
        if (service != null) sb.append(" | Service ").append(service);
        return !sb.isEmpty() ? sb.toString() : "Tous les incidents";
    }
}