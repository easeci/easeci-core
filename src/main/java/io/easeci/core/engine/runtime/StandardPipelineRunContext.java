package io.easeci.core.engine.runtime;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static io.easeci.core.engine.runtime.PipelineRunStatus.PIPELINE_EXEC_STARTED;

@Slf4j
public class StandardPipelineRunContext implements PipelineRunContext {

    @Override
    public PipelineRunStatus runPipeline(UUID pipelineId) {
        log.error("Method not implemented, running pipeline: {}", pipelineId);
        return PIPELINE_EXEC_STARTED;
    }
}
