package com.firststudent.assignments.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.firststudent.assignments.domain.Assignment;
import com.firststudent.assignments.domain.AssignmentKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies keys/args passed to Redis without a live server. Pair with ElastiCache integration tests
 * in your environment or Docker-based CI when available.
 */
@ExtendWith(MockitoExtension.class)
class LettuceAssignmentStoreTest {

    @Mock
    StringRedisTemplate redis;

    @Captor
    ArgumentCaptor<List<String>> keysCaptor;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    LettuceAssignmentStore store;

    @BeforeEach
    void setUp() {
        store = new LettuceAssignmentStore(redis, objectMapper);
    }

    @Test
    void upsert_runsLuaWithHashAndIndexKeys() {
        when(redis.execute(any(DefaultRedisScript.class), any(List.class), any())).thenReturn(1L);

        Assignment a = new Assignment("t1", "d1", "v1", null, Map.of("k", "v"));
        store.upsert(a);

        verify(redis).execute(any(DefaultRedisScript.class), keysCaptor.capture(), any());
        assertThat(keysCaptor.getValue()).containsExactly(
                AssignmentKeys.primaryHashKey("t1", "d1", "v1"),
                AssignmentKeys.byTaskKey("t1"),
                AssignmentKeys.byDriverKey("d1"),
                AssignmentKeys.byVehicleKey("v1"));
    }

    @Test
    void delete_runsLuaWithSameKeys() {
        when(redis.execute(any(DefaultRedisScript.class), any(List.class))).thenReturn(1L);

        store.delete("t1", "d1", "v1");

        verify(redis).execute(any(DefaultRedisScript.class), keysCaptor.capture());
        assertThat(keysCaptor.getValue()).containsExactly(
                AssignmentKeys.primaryHashKey("t1", "d1", "v1"),
                AssignmentKeys.byTaskKey("t1"),
                AssignmentKeys.byDriverKey("d1"),
                AssignmentKeys.byVehicleKey("v1"));
    }
}
