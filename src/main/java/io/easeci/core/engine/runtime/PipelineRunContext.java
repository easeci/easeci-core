package io.easeci.core.engine.runtime;

import java.util.UUID;

/**
 * Main entry-point interface to whole Pipeline running logic.
 * Instance of this class should be created every time when
 * pipeline is need to run. PipelineRunContext should gather and hold
 * whole context, information and required parameters of pipeline execution.
 * @author Karol Meksuła
 * 2021-04-01
 * */
public interface PipelineRunContext {

    /**
     * Use this method to start pipeline execution.
     * @param pipelineId is an id in UUID format from project-structure.json file
     * @return status that inform about initiate an pipeline execution
     * */
    PipelineRunStatus runPipeline(UUID pipelineId);
}
