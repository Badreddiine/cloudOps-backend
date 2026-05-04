package io.cloudops.notificationservice.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String userId;
    private String type;
    private String message;
    private Long incidentId;
    private Boolean read;
    private LocalDateTime createdAt;
}
