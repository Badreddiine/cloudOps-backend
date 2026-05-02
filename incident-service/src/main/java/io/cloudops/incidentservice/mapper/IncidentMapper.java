package io.cloudops.incidentservice.mapper;

import io.cloudops.incidentservice.dto.response.AuditLogResponse;
import io.cloudops.incidentservice.dto.response.IncidentResponse;
import io.cloudops.incidentservice.entity.AuditLog;
import io.cloudops.incidentservice.entity.Incident;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;
@Mapper(componentModel = "spring")
public interface IncidentMapper {
    IncidentMapper INSTANCE = Mappers.getMapper(IncidentMapper.class);
    @Mapping(target = "auditLogs", source = "auditLogs")
    IncidentResponse toIncidentResponse(Incident incident);
    AuditLogResponse toAuditLogResponse(AuditLog auditLog);
    List<IncidentResponse> toIncidentResponseList(List<Incident> incidents);
}
