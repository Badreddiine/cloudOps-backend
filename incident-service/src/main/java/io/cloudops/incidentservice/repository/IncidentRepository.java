package io.cloudops.incidentservice.repository;

import io.cloudops.incidentservice.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident>
{
}
