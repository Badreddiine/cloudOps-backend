package io.cloudops.incidentservice.dto.request;

import io.cloudops.incidentservice.entity.IncidentCategory;
import io.cloudops.incidentservice.entity.IncidentPriority;
import io.cloudops.incidentservice.entity.IncidentStatus;
import lombok.Data;

@Data
public class UpdateIncidentRequest {
    private String title;
    private String description;
    private IncidentPriority priority;
    private IncidentCategory category;
    private IncidentStatus status;
    private String assignedTo;
}
