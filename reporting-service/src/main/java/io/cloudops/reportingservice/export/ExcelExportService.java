package io.cloudops.reportingservice.export;

import io.cloudops.reportingservice.dto.ReportRowDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Export Excel (XLSX) avec Apache POI.
 * Génère un fichier avec header stylisé, colonnes auto-sized,
 * et coloration conditionnelle selon la priorité.
 */
@Component
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── En-têtes colonnes ─────────────────────────────────────────────────
    private static final String[] HEADERS = {
        "ID", "Titre", "Priorité", "Statut",
        "Service impacté", "Équipe", "Assigné à",
        "Créé le", "Résolu le", "Durée résolution (min)", "SLA respecté"
    };

    public byte[] exportIncidents(List<ReportRowDTO> incidents, String title) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Incidents");

            // ── Styles ────────────────────────────────────────────────────
            CellStyle titleStyle   = createTitleStyle(workbook);
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle criticalStyle = createPriorityStyle(workbook, IndexedColors.RED);
            CellStyle highStyle    = createPriorityStyle(workbook, IndexedColors.ORANGE);
            CellStyle normalStyle  = createNormalStyle(workbook);
            CellStyle dateStyle    = createDateStyle(workbook);

            // ── Titre du rapport (ligne 0, fusionnée) ─────────────────────
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // ── Ligne d'en-tête (ligne 1) ─────────────────────────────────
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Données (à partir de ligne 2) ─────────────────────────────
            int rowNum = 2;
            for (ReportRowDTO row : incidents) {
                Row dataRow = sheet.createRow(rowNum++);

                CellStyle rowStyle = switch (row.getPriority()) {
                    case "CRITICAL" -> criticalStyle;
                    case "HIGH"     -> highStyle;
                    default         -> normalStyle;
                };

                createCell(dataRow, 0, row.getIncidentId()   != null ? row.getIncidentId().toString() : "",  rowStyle);
                createCell(dataRow, 1, row.getTitle()         != null ? row.getTitle()        : "",          rowStyle);
                createCell(dataRow, 2, row.getPriority()      != null ? row.getPriority()     : "",          rowStyle);
                createCell(dataRow, 3, row.getStatus()        != null ? row.getStatus()       : "",          rowStyle);
                createCell(dataRow, 4, row.getServiceImpacted()!= null? row.getServiceImpacted(): "",        rowStyle);
                createCell(dataRow, 5, row.getTeamName()      != null ? row.getTeamName()     : "",          rowStyle);
                createCell(dataRow, 6, row.getAssignedTo()    != null ? row.getAssignedTo()   : "",          rowStyle);
                createCell(dataRow, 7,
                    row.getCreatedAt()  != null ? row.getCreatedAt().format(FMT)  : "", dateStyle);
                createCell(dataRow, 8,
                    row.getResolvedAt() != null ? row.getResolvedAt().format(FMT) : "—", dateStyle);
                createNumericCell(dataRow, 9,
                    row.getResolutionTimeMinutes() != null ? row.getResolutionTimeMinutes() : 0L, rowStyle);
                createCell(dataRow, 10, row.isSlaBreached() ? "NON ❌" : "OUI ✓", rowStyle);
            }

            // ── Auto-resize colonnes ───────────────────────────────────────
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // min 80px, max 200px (en unités POI : 256 * caractères)
                int width = Math.max(sheet.getColumnWidth(i), 80 * 36);
                sheet.setColumnWidth(i, Math.min(width, 200 * 36));
            }

            // ── Figer la ligne d'en-tête ───────────────────────────────────
            sheet.createFreezePane(0, 2);

            // ── Filtre automatique ─────────────────────────────────────────
            sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, HEADERS.length - 1));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("[Export] Excel généré : {} lignes", incidents.size());
            return out.toByteArray();
        }
    }

    // ── Helpers styles ─────────────────────────────────────────────────────

    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)30,(byte)78,(byte)121}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)68,(byte)114,(byte)196}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createPriorityStyle(XSSFWorkbook wb, IndexedColors color) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNormalStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, long value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
