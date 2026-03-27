package com.firststudent.assignments.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentKeysTest {

    @Test
    void primaryAndIndexes_followConvention() {
        assertThat(AssignmentKeys.primaryHashKey("t", "d", "v")).isEqualTo("assignments:t:d:v");
        assertThat(AssignmentKeys.byTaskKey("t")).isEqualTo("assignments:by_task:t");
        assertThat(AssignmentKeys.byDriverKey("d")).isEqualTo("assignments:by_driver:d");
        assertThat(AssignmentKeys.byVehicleKey("v")).isEqualTo("assignments:by_vehicle:v");
    }

    @Test
    void rejectsColonInIds() {
        assertThatThrownBy(() -> AssignmentKeys.validateNoDelimiter("a:b", "c", "d"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
