package com.firststudent.assignments.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.assignments.domain.Assignment;
import com.firststudent.assignments.domain.AssignmentKeys;
import com.firststudent.assignments.redis.lua.AssignmentLuaScripts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class LettuceAssignmentStore implements AssignmentStore {

    private static final Logger log = LoggerFactory.getLogger(LettuceAssignmentStore.class);
    private static final String PAYLOAD_FIELD = "payload";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> upsertScript;
    private final DefaultRedisScript<Long> deleteScript;

    public LettuceAssignmentStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.upsertScript = new DefaultRedisScript<>();
        this.upsertScript.setScriptText(AssignmentLuaScripts.UPSERT);
        this.upsertScript.setResultType(Long.class);
        this.deleteScript = new DefaultRedisScript<>();
        this.deleteScript.setScriptText(AssignmentLuaScripts.DELETE);
        this.deleteScript.setResultType(Long.class);
    }

    @Override
    public void upsert(Assignment assignment) {
        String json;
        try {
            json = objectMapper.writeValueAsString(assignment);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Assignment", e);
        }
        String hashKey = AssignmentKeys.primaryHashKey(assignment.taskId(), assignment.driverId(), assignment.vehicleId());
        List<String> keys = List.of(
                hashKey,
                AssignmentKeys.byTaskKey(assignment.taskId()),
                AssignmentKeys.byDriverKey(assignment.driverId()),
                AssignmentKeys.byVehicleKey(assignment.vehicleId()));
        Long result = redis.execute(upsertScript, keys, json);
        if (result == null || result != 1L) {
            log.warn("Unexpected upsert script result: {}", result);
        }
    }

    @Override
    public void delete(String taskId, String driverId, String vehicleId) {
        AssignmentKeys.validateNoDelimiter(taskId, driverId, vehicleId);
        String hashKey = AssignmentKeys.primaryHashKey(taskId, driverId, vehicleId);
        List<String> keys = List.of(
                hashKey,
                AssignmentKeys.byTaskKey(taskId),
                AssignmentKeys.byDriverKey(driverId),
                AssignmentKeys.byVehicleKey(vehicleId));
        redis.execute(deleteScript, keys);
    }

    @Override
    public Optional<Assignment> findByCompositeKey(String taskId, String driverId, String vehicleId) {
        AssignmentKeys.validateNoDelimiter(taskId, driverId, vehicleId);
        String hashKey = AssignmentKeys.primaryHashKey(taskId, driverId, vehicleId);
        String payload = payloadAt(hashKey);
        return parse(payload);
    }

    @Override
    public List<Assignment> findByTaskId(String taskId) {
        return loadFromSetIndex(AssignmentKeys.byTaskKey(taskId));
    }

    @Override
    public List<Assignment> findByDriverId(String driverId) {
        return loadFromSetIndex(AssignmentKeys.byDriverKey(driverId));
    }

    @Override
    public List<Assignment> findByVehicleId(String vehicleId) {
        return loadFromSetIndex(AssignmentKeys.byVehicleKey(vehicleId));
    }

    private List<Assignment> loadFromSetIndex(String setKey) {
        Set<String> members = redis.opsForSet().members(setKey);
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<Assignment> out = new ArrayList<>(members.size());
        for (String hashKey : members) {
            String payload = payloadAt(hashKey);
            parse(payload).ifPresent(out::add);
        }
        return Collections.unmodifiableList(out);
    }

    private String payloadAt(String hashKey) {
        Object raw = redis.opsForHash().get(hashKey, PAYLOAD_FIELD);
        return raw == null ? null : raw.toString();
    }

    private Optional<Assignment> parse(String payload) {
        if (payload == null || payload.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, Assignment.class));
        } catch (JsonProcessingException e) {
            log.warn("Ignoring corrupt assignment payload: {}", e.toString());
            return Optional.empty();
        }
    }
}
