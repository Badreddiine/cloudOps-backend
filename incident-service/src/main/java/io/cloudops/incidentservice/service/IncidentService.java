package io.cloudops.incidentservice.service;

import io.cloudops.incidentservice.dto.request.CreateIncidentRequest;
import io.cloudops.incidentservice.dto.request.UpdateIncidentRequest;
import io.cloudops.incidentservice.dto.response.IncidentResponse;

import java.util.List;

public interface IncidentService {
    IncidentResponse createIncident(CreateIncidentRequest request, String reportedBy);
    IncidentResponse findIncidentById(Long id);
    List<IncidentResponse> findAllIncidents();
    IncidentResponse updateIncident(Long id, UpdateIncidentRequest request, String lastModifiedBy);
    void deleteIncident(Long id);
    void markSlaBreached(Long incidentId);
}
