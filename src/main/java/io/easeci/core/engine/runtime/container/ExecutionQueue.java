package io.easeci.core.engine.runtime.container;

import io.easeci.core.engine.runtime.commons.PipelineRunStatus;

import java.util.UUID;

/**
 * Main entry-point interface to put Pipeline execution to queue.
 * @author Karol Meksuła
 * 2021-04-01
 * */
public interface ExecutionQueue {

    /**
     * Use this method to put pipeline execution on queue and wait for execution in scheduled time.
     * @param pipelineId is an id in UUID format from project-structure.json file
     * @return status that inform about initiate an pipeline execution
     * */
    PipelineRunStatus runPipeline(UUID pipelineId);
}
