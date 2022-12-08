package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;

import java.util.Optional;

public interface PipelineExecutionQueue {

    /**
     * Pass your PipelineContext here - main pipeline runtime entity and put this on queue
     * @return true if PipelineContext was queued with success
     * or false when PipelineContext was not queued for some reason
     * */
    boolean put(PipelineContext pipelineContext);

    /**
     * Get next PipelineContext from the top of queue,
     * that should be processing next
     * */
    Optional<PipelineContext> next();

    boolean isEmpty();
}
