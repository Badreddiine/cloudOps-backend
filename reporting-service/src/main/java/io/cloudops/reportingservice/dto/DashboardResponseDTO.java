package io.cloudops.reportingservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private KpiDTO              kpis;
    private List<ChartPointDTO> byPriority;
    private List<ChartPointDTO> byStatus;
    private List<TrendPointDTO> trend7Days;
    private List<TrendPointDTO> trend30Days;
    private List<TeamStatDTO>   byTeam;
    private LocalDateTime       generatedAt;
}
