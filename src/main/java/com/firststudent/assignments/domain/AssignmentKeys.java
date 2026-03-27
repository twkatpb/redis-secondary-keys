package com.firststudent.assignments.domain;

/**
 * Key layout (Redis OSS, no Stack):
 * <ul>
 *     <li>Hash (document): {@code assignments:{taskId}:{driverId}:{vehicleId}}</li>
 *     <li>Set index: {@code assignments:by_task:{taskId}} → members are full hash keys</li>
 *     <li>Set index: {@code assignments:by_driver:{driverId}}</li>
 *     <li>Set index: {@code assignments:by_vehicle:{vehicleId}}</li>
 * </ul>
 * <p>
 * IDs must not contain {@value #DELIMITER} so keys stay unambiguous without escaping.
 * If you need arbitrary strings, hash or base64-url the three components before calling this layer.
 */
public final class AssignmentKeys {

    public static final String PREFIX = "assignments";
    public static final char DELIMITER = ':';

    private AssignmentKeys() {
    }

    public static void validateNoDelimiter(String taskId, String driverId, String vehicleId) {
        if (containsDelimiter(taskId) || containsDelimiter(driverId) || containsDelimiter(vehicleId)) {
            throw new IllegalArgumentException(
                    "taskId, driverId, and vehicleId must not contain ':'; encode IDs first");
        }
    }

    public static boolean containsDelimiter(String value) {
        return value.indexOf(DELIMITER) >= 0;
    }

    public static String primaryHashKey(String taskId, String driverId, String vehicleId) {
        return PREFIX + DELIMITER + taskId + DELIMITER + driverId + DELIMITER + vehicleId;
    }

    public static String byTaskKey(String taskId) {
        return PREFIX + DELIMITER + "by_task" + DELIMITER + taskId;
    }

    public static String byDriverKey(String driverId) {
        return PREFIX + DELIMITER + "by_driver" + DELIMITER + driverId;
    }

    public static String byVehicleKey(String vehicleId) {
        return PREFIX + DELIMITER + "by_vehicle" + DELIMITER + vehicleId;
    }
}
