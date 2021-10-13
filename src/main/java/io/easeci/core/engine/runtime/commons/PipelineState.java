package io.easeci.core.engine.runtime.commons;

public enum PipelineState {
    NEW,
    CLOSED,
    WAITING_FOR_SCHEDULE,
    ABORTED_PREPARATION_ERROR,
    ABORTED_CRITICAL_ERROR,
    SCHEDULED,
    ARCHIVED
}
