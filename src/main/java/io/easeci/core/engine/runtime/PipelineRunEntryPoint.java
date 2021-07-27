package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import io.easeci.core.engine.runtime.logs.LogRail;

import java.util.List;
import java.util.UUID;

/**
 * Main interface of this package. Entry point for pipeline runtime.
 * You can call methods in this interface to start pipeline or check pipeline execution state.
 * For example you can use instance of this class to start pipeline from HTTP controller etc.
 * @author Karol Meksuła
 * 2021-04-24
 * */
public interface PipelineRunEntryPoint {

    /**
     * Use this method to start pipeline created after Easefile parse process.
     * @param pipelineId is id of pipeline defined in project-structure.json
     *                   Pipeline must exists in system, must be added before.
     * @return status of pipeline execution with pipelineContextId. Inform us is pipeline started or not.
     * */
    PipelineRunStatus.PipelineRunStatusWrapper runPipeline(UUID pipelineId);

    /**
     * Use this method to check state of Pipeline runtime.
     * @return list of object with basic fields informing us about state of Pipeline runtime.
     * */
    List<PipelineContextState> contextQueueState();

    /**
     * Use it to find proxy object that allows to read in stream way logs of Pipeline execution.
     * Notice that `pipelineId` and `pipelineContextId` are not the same.
     * @return LogRail that can be use to read logs of PipelineContext (runtime of pipeline)
     * */
    LogRail getLogRail(UUID pipelineContextId);

    /**
     * Use it to get LogRail object that is able to streaming historical logs to client.
     * @return LogRail that can be use to read logs of historical PipelineContext
     * */
    LogRail getFileLogRail(UUID pipelineContextId);
}
