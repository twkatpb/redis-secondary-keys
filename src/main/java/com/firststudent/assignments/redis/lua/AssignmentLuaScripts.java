package com.firststudent.assignments.redis.lua;

/**
 * Atomic upsert/delete for Hash + three Set indexes. Safe on single-shard / non-cluster Redis.
 * Redis Cluster: all keys must hash to the same slot for {@code EVAL} — these keys generally
 * will not; use non-cluster ElastiCache or accept non-atomic index maintenance via application-level retry.
 */
public final class AssignmentLuaScripts {

    public static final String UPSERT = """
            local hashKey = KEYS[1]
            local byTask = KEYS[2]
            local byDriver = KEYS[3]
            local byVehicle = KEYS[4]
            local payload = ARGV[1]
            redis.call('HSET', hashKey, 'payload', payload)
            redis.call('SADD', byTask, hashKey)
            redis.call('SADD', byDriver, hashKey)
            redis.call('SADD', byVehicle, hashKey)
            return 1
            """;

    public static final String DELETE = """
            local hashKey = KEYS[1]
            local byTask = KEYS[2]
            local byDriver = KEYS[3]
            local byVehicle = KEYS[4]
            redis.call('SREM', byTask, hashKey)
            redis.call('SREM', byDriver, hashKey)
            redis.call('SREM', byVehicle, hashKey)
            redis.call('DEL', hashKey)
            return 1
            """;

    private AssignmentLuaScripts() {
    }
}
