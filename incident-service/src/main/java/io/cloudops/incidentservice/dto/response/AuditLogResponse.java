package io.cloudops.incidentservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private String action;
    private String oldValue;
    private String newValue;
    private String performedBy;
    private LocalDateTime timestamp;
}
