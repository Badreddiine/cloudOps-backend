package io.cloudops.incidentservice.dto.request;

import io.cloudops.incidentservice.entity.IncidentCategory;
import io.cloudops.incidentservice.entity.IncidentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIncidentRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;
    @NotBlank(message = "Description cannot be empty")
    private String description;
    @NotNull(message = "Priority cannot be null")
    private IncidentPriority priority;
    @NotNull(message = "Category cannot be null")
    private IncidentCategory category;
    private String assignedTo;
}
