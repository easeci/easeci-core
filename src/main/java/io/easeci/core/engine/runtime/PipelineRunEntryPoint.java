package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;

import java.util.List;
import java.util.UUID;

public interface PipelineRunEntryPoint {

    PipelineRunStatus runPipeline(UUID pipelineId);

    List<PipelineContextState> contextQueueState();
}
