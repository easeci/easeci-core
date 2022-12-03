package io.easeci.core.engine.runtime.commons;

/**
 * Enum class that define state of pipeline in which PipelineContext could be with.
 * @author Karol Meksuła
 * 2021-04-12
 * */
public enum PipelineState {

    /**
     * NEW means that PipelineContext run is just started and nothing else was not occurred.
     * */
    NEW,

    /**
     * CLOSED means that PipelineContext's work was finished and application cannot operate on this one no longer.
     * */
    CLOSED,

    /**
     * WAITING_FOR_SCHEDULE means that PipelineContext is ready for run on some worker node
     * */
    WAITING_FOR_SCHEDULE,

    /**
     * QUEUED means that PipelineContext is ready but no Worker Node is available for Pipeline processing
     * so Pipeline must wait on queue
     * */
    QUEUED,

    /**
     * READY_FOR_SCHEDULE means that PipelineContext is ready for run on some worker node.
     * PipelineContextReadinessValidator confirmed that PipelineContext is correct and valid.
     * */
    READY_FOR_SCHEDULE,

    /**
     * ABORTED_PREPARATION_ERROR means that Pipeline run was end with failure because of some domain error.
     * For example: Variables cannot resolved, Performer returns nulls etc.
     * */
    ABORTED_PREPARATION_ERROR,

    /**
     * ABORTED_CRITICAL_ERROR means that Pipeline run was end with unexpected system error.
     * For example: some IO exception, NullPointerException etc.
     * */
    ABORTED_CRITICAL_ERROR,

    /**
     * VALIDATION_ERROR means that Pipeline was build correctly but PipelineContextReadinessValidator do not pass
     * pipeline to further processing and scheduling on worker node.
     * */
    VALIDATION_ERROR,

    /**
     * SCHEDULED means that PipelineContext was assigned with success to some worker node.
     * */
    SCHEDULED
}
