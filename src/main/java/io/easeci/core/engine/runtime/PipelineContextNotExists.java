package io.easeci.core.engine.runtime;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class PipelineContextNotExists extends RuntimeException {

    private UUID pipelineContextId;

    public PipelineContextNotExists(UUID pipelineContextId) {
        this.pipelineContextId = pipelineContextId;
        log.error(this.getMessage());
    }

    @Override
    public String getMessage() {
        return "PipelineContext with id: " + this.pipelineContextId + " not exists in system";
    }
}
