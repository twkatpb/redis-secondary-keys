package com.firststudent.assignments.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Assignment(
        String taskId,
        String driverId,
        String vehicleId,
        Instant updatedAt,
        Map<String, String> attributes
) {

    public Assignment {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(driverId, "driverId");
        Objects.requireNonNull(vehicleId, "vehicleId");
        if (taskId.isBlank() || driverId.isBlank() || vehicleId.isBlank()) {
            throw new IllegalArgumentException("taskId, driverId, and vehicleId must be non-blank");
        }
        AssignmentKeys.validateNoDelimiter(taskId, driverId, vehicleId);
        updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static Assignment fromEvent(String taskId, String driverId, String vehicleId) {
        return new Assignment(taskId, driverId, vehicleId, Instant.now(), Map.of());
    }
}
