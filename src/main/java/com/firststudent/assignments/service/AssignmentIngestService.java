package com.firststudent.assignments.service;

import com.firststudent.assignments.domain.Assignment;
import com.firststudent.assignments.kafka.AssignmentEvent;
import com.firststudent.assignments.redis.AssignmentStore;
import org.springframework.stereotype.Service;

@Service
public class AssignmentIngestService {

    private final AssignmentStore assignmentStore;

    public AssignmentIngestService(AssignmentStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    public void apply(AssignmentEvent event) {
        AssignmentEvent.Operation op = event.resolveOperation();
        if (op == AssignmentEvent.Operation.DELETE) {
            assignmentStore.delete(event.taskId(), event.driverId(), event.vehicleId());
            return;
        }
        Assignment assignment = event.toAssignment();
        assignmentStore.upsert(assignment);
    }
}
