package io.cloudops.incidentservice.controller;


import io.cloudops.incidentservice.dto.request.CreateIncidentRequest;
import io.cloudops.incidentservice.dto.request.UpdateIncidentRequest;
import io.cloudops.incidentservice.dto.response.IncidentResponse;
import io.cloudops.incidentservice.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService incidentService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'DEVOPS\', \'SUPPORT\')")
    public IncidentResponse createIncident(@Valid @RequestBody CreateIncidentRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        String reportedBy = jwt.getSubject();
        return incidentService.createIncident(request, reportedBy);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'DEVOPS\', \'SUPPORT\', \'VIEWER\')")
    public List<IncidentResponse> getAllIncidents() {
        return incidentService.findAllIncidents();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'DEVOPS\', \'SUPPORT\', \'VIEWER\')")
    public IncidentResponse getIncidentById(@PathVariable Long id) {
        return incidentService.findIncidentById(id);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'DEVOPS\', \'SUPPORT\')")
    public IncidentResponse updateIncident(@PathVariable Long id, @Valid @RequestBody UpdateIncidentRequest
            request, @AuthenticationPrincipal Jwt jwt) {
        String lastModifiedBy = jwt.getSubject();
        return incidentService.updateIncident(id, request, lastModifiedBy);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole(\'ADMIN\')")
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }
}

