package com.firststudent.assignments.redis;

import com.firststudent.assignments.domain.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentStore {

    void upsert(Assignment assignment);

    void delete(String taskId, String driverId, String vehicleId);

    Optional<Assignment> findByCompositeKey(String taskId, String driverId, String vehicleId);

    List<Assignment> findByTaskId(String taskId);

    List<Assignment> findByDriverId(String driverId);

    List<Assignment> findByVehicleId(String vehicleId);
}
