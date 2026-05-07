package io.cloudops.reportingservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartPointDTO {
    private String label;
    private long   value;
}
