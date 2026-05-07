package io.cloudops.reportingservice.export;


import io.cloudops.reportingservice.dto.ReportRowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Export CSV simple — pas de dépendance externe, UTF-8 avec BOM
 * pour ouverture directe dans Excel (français).
 */
@Component
@Slf4j
public class CsvExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportIncidents(List<ReportRowDTO> incidents) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // BOM UTF-8 pour Excel français
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // En-tête
            pw.println(String.join(";",
                "ID", "Titre", "Priorité", "Statut",
                "Service impacté", "Équipe", "Assigné à",
                "Créé le", "Résolu le", "Durée résolution (min)", "SLA respecté"
            ));

            // Données
            for (ReportRowDTO row : incidents) {
                pw.println(String.join(";",
                    safe(row.getIncidentId()          != null ? row.getIncidentId().toString() : ""),
                    safe(row.getTitle()),
                    safe(row.getPriority()),
                    safe(row.getStatus()),
                    safe(row.getServiceImpacted()),
                    safe(row.getTeamName()),
                    safe(row.getAssignedTo()),
                    safe(row.getCreatedAt()           != null ? row.getCreatedAt().format(FMT)  : ""),
                    safe(row.getResolvedAt()           != null ? row.getResolvedAt().format(FMT) : ""),
                    safe(row.getResolutionTimeMinutes() != null ? row.getResolutionTimeMinutes().toString() : ""),
                    row.isSlaBreached() ? "NON" : "OUI"
                ));
            }
        }

        log.info("[Export] CSV généré : {} lignes", incidents.size());
        return baos.toByteArray();
    }

    /** Échappe les champs CSV contenant des ; ou des guillemets */
    private String safe(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
