package io.cloudops.reportingservice.export;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import io.cloudops.reportingservice.dto.MonthlyReportDTO;
import io.cloudops.reportingservice.dto.ReportRowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Export PDF du rapport mensuel avec iText 8.
 * Génère un rapport structuré avec : page de titre, KPIs,
 * tableau des incidents, footer paginé.
 */
@Component
@Slf4j
public class PdfExportService {

    private static final DeviceRgb PRIMARY_COLOR  = new DeviceRgb(30, 78, 121);
    private static final DeviceRgb HEADER_COLOR   = new DeviceRgb(68, 114, 196);
    private static final DeviceRgb CRITICAL_COLOR = new DeviceRgb(255, 99, 71);
    private static final DeviceRgb HIGH_COLOR      = new DeviceRgb(255, 165, 0);
    private static final DeviceRgb SUCCESS_COLOR   = new DeviceRgb(70, 180, 70);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateMonthlyReport(MonthlyReportDTO report) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter   writer      = new PdfWriter(out);
        PdfDocument pdfDoc      = new PdfDocument(writer);
        Document    document    = new Document(pdfDoc, PageSize.A4.rotate()); // Paysage pour le tableau

        document.setMargins(30, 30, 30, 30);

        // ── En-tête du rapport ─────────────────────────────────────────────
        addTitle(document, "Rapport Mensuel CloudOps — " + report.getMonth());
        addSubtitle(document, "CloudOps Incident Manager | Généré automatiquement");
        document.add(new Paragraph("\n"));

        // ── KPIs résumé ────────────────────────────────────────────────────
        addKpiSection(document, report);
        document.add(new Paragraph("\n"));

        // ── Tableau des incidents ──────────────────────────────────────────
        addSectionTitle(document, "Détail des incidents du mois");
        addIncidentsTable(document, report.getIncidents());

        // ── Footer ────────────────────────────────────────────────────────
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("CloudOps Incident Manager — Rapport confidentiel")
            .setFontSize(8)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);

        document.close();
        log.info("[Export] PDF mensuel généré : {} incidents", report.getIncidents().size());
        return out.toByteArray();
    }

    // ── Rapport sur incidents filtrés (pour export à la demande) ──────────

    public byte[] generateFilteredReport(List<ReportRowDTO> incidents, String filterDescription) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
        Document    document = new Document(pdfDoc, PageSize.A4.rotate());
        document.setMargins(30, 30, 30, 30);

        addTitle(document, "Rapport Incidents — CloudOps");
        addSubtitle(document, "Filtre : " + filterDescription);
        document.add(new Paragraph("\n"));

        addIncidentsTable(document, incidents);
        document.close();
        return out.toByteArray();
    }

    // ── Sections ──────────────────────────────────────────────────────────

    private void addTitle(Document doc, String text) {
        Paragraph p = new Paragraph(text)
            .setFontSize(18)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(p);
    }

    private void addSubtitle(Document doc, String text) {
        Paragraph p = new Paragraph(text)
            .setFontSize(10)
            .setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER);
        doc.add(p);
    }

    private void addSectionTitle(Document doc, String text) {
        Paragraph p = new Paragraph(text)
            .setFontSize(13)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setMarginBottom(8);
        doc.add(p);
    }

    private void addKpiSection(Document doc, MonthlyReportDTO r) {
        addSectionTitle(doc, "Résumé du mois");

        // Tableau KPI 2x4
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2, 1}))
            .useAllAvailableWidth();

        addKpiCell(table, "Total incidents",   String.valueOf(r.getTotal()));
        addKpiCell(table, "Résolus",           String.valueOf(r.getResolvedCount()));
        addKpiCell(table, "CRITICAL",          String.valueOf(r.getCriticalCount()));
        addKpiCell(table, "HIGH",              String.valueOf(r.getHighCount()));
        addKpiCell(table, "SLA compliance",    r.getSlaComplianceRate() + " %");
        addKpiCell(table, "SLA dépassés",      String.valueOf(r.getSlaBreachedCount()));
        addKpiCell(table, "Durée moy. résol.", r.getAvgResolutionMinutes() + " min");
        addKpiCell(table, "MEDIUM / LOW",
            r.getMediumCount() + " / " + r.getLowCount());

        doc.add(table);
    }

    private void addKpiCell(Table table, String label, String value) {
        // Cellule label
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setFontSize(9).setFontColor(ColorConstants.GRAY))
            .setBorder(null)
            .setPadding(6);
        table.addCell(labelCell);

        // Cellule valeur
        Cell valueCell = new Cell()
            .add(new Paragraph(value).setFontSize(14).setBold().setFontColor(PRIMARY_COLOR))
            .setBorder(null)
            .setPadding(6);
        table.addCell(valueCell);
    }

    private void addIncidentsTable(Document doc, List<ReportRowDTO> incidents) {
        // 9 colonnes en paysage A4
        float[] colWidths = {3, 12, 5, 6, 8, 7, 7, 8, 5};
        Table table = new Table(UnitValue.createPercentArray(colWidths))
            .useAllAvailableWidth()
            .setFontSize(8);

        // Header
        String[] headers = {"ID", "Titre", "Priorité", "Statut", "Service", "Équipe", "Créé le", "Résolu le", "SLA"};
        for (String h : headers) {
            table.addHeaderCell(
                new Cell()
                    .add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setPadding(5)
            );
        }

        // Données
        for (ReportRowDTO row : incidents) {
            DeviceRgb rowColor = switch (row.getPriority() != null ? row.getPriority() : "") {
                case "CRITICAL" -> new DeviceRgb(255, 235, 230);
                case "HIGH"     -> new DeviceRgb(255, 243, 220);
                default         -> new DeviceRgb(255, 255, 255);
            };

            addTableCell(table, row.getIncidentId()      != null ? row.getIncidentId().toString() : "",   rowColor);
            addTableCell(table, truncate(row.getTitle(), 40),                                              rowColor);
            addPriorityCell(table, row.getPriority());
            addTableCell(table, row.getStatus()          != null ? row.getStatus()          : "",         rowColor);
            addTableCell(table, truncate(row.getServiceImpacted(), 20),                                    rowColor);
            addTableCell(table, row.getTeamName()        != null ? row.getTeamName()        : "",         rowColor);
            addTableCell(table, row.getCreatedAt()       != null ? row.getCreatedAt().format(FMT)  : "",  rowColor);
            addTableCell(table, row.getResolvedAt()      != null ? row.getResolvedAt().format(FMT) : "—", rowColor);

            // SLA cell colorisée
            Cell slaCell = new Cell()
                .add(new Paragraph(row.isSlaBreached() ? "NON" : "OUI")
                    .setBold()
                    .setFontColor(row.isSlaBreached() ? CRITICAL_COLOR : SUCCESS_COLOR))
                .setBackgroundColor(rowColor)
                .setPadding(4);
            table.addCell(slaCell);
        }

        doc.add(table);
    }

    private void addTableCell(Table table, String value, DeviceRgb bgColor) {
        table.addCell(
            new Cell()
                .add(new Paragraph(value != null ? value : ""))
                .setBackgroundColor(bgColor)
                .setPadding(4)
        );
    }

    private void addPriorityCell(Table table, String priority) {
        DeviceRgb color = switch (priority != null ? priority : "") {
            case "CRITICAL" -> CRITICAL_COLOR;
            case "HIGH"     -> HIGH_COLOR;
            case "MEDIUM"   -> new DeviceRgb(100, 149, 237);
            default         -> new DeviceRgb(100, 180, 100);
        };
        table.addCell(
            new Cell()
                .add(new Paragraph(priority != null ? priority : "")
                    .setBold().setFontColor(color))
                .setPadding(4)
        );
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
