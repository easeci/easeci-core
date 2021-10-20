package io.easeci.core.engine.pipeline;

public enum ExecutingStrategy {
//    execute pipeline only on master instance - ignore all nodeUuids and names
    MASTER, // todo to remove - master node not executing pipelines
//    execute pipeline at any idling instance - ignore all nodeUuids and names
    AUTO,
//    execute pipeline on any typed instance in nodeUuids or names
    ONE_OF,
//    execute pipeline on every typed instance in nodeUuids or names
    EACH;

    public static ExecutingStrategy fromString(String strategy) {
        if (strategy == null) {
            return AUTO;
        }
        return ExecutingStrategy.valueOf(strategy.toUpperCase());
    }
}
