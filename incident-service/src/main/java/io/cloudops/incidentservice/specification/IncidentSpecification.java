package io.cloudops.incidentservice.specification;

import io.cloudops.incidentservice.entity.Incident;
import io.cloudops.incidentservice.entity.IncidentPriority;
import io.cloudops.incidentservice.entity.IncidentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class IncidentSpecification {
    public static Specification<Incident> withFilters(String title, IncidentStatus status, IncidentPriority
            priority, String assignedTo, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" +
                        title.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (assignedTo != null && !assignedTo.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("assignedTo")),
                        assignedTo.toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

