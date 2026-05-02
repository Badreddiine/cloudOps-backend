package io.cloudops.incidentservice.statemachine;


import io.cloudops.incidentservice.entity.IncidentStatus;
import io.cloudops.incidentservice.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
@Component
public class IncidentStateMachine {
    // Matrice des transitions autorisées
    private static final Map<IncidentStatus, Set<IncidentStatus>> TRANSITIONS = Map.of(
            IncidentStatus.OPEN, Set.of(IncidentStatus.IN_PROGRESS),
            IncidentStatus.IN_PROGRESS, Set.of(IncidentStatus.RESOLVED),
            IncidentStatus.RESOLVED, Set.of(IncidentStatus.CLOSED, IncidentStatus.REOPENED),
            IncidentStatus.CLOSED, Set.of(IncidentStatus.REOPENED),
            IncidentStatus.REOPENED, Set.of(IncidentStatus.IN_PROGRESS)
    );

    public void validateTransition(IncidentStatus current, IncidentStatus next) {
        Set<IncidentStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidStatusTransitionException(
                    "Transition " + current + " -> " + next + " is not allowed"
            );
        }
    }
}
