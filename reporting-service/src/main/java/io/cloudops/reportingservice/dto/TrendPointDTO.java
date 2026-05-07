package io.cloudops.reportingservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointDTO {
    private String date;
    private long   count;
}
