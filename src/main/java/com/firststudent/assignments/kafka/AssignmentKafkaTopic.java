package com.firststudent.assignments.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("assignmentKafkaTopic")
public class AssignmentKafkaTopic {

    private final String name;

    public AssignmentKafkaTopic(@Value("${app.kafka.assignment-topic}") String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
