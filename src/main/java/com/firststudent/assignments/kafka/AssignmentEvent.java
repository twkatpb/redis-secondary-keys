package com.firststudent.assignments.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firststudent.assignments.domain.Assignment;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssignmentEvent(String taskId, String driverId, String vehicleId, String operation) {

    public enum Operation {
        UPSERT,
        DELETE
    }

    public Assignment toAssignment() {
        return Assignment.fromEvent(taskId, driverId, vehicleId);
    }

    public Operation resolveOperation() {
        if (operation == null || operation.isBlank()) {
            return Operation.UPSERT;
        }
        try {
            return Operation.valueOf(operation.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("operation must be UPSERT or DELETE, got: " + operation);
        }
    }
}
