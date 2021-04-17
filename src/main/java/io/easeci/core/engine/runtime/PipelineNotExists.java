package io.easeci.core.engine.runtime;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class PipelineNotExists extends Exception {

    private UUID pipelineId;

    public PipelineNotExists(UUID pipelineId) {
        this.pipelineId = pipelineId;
        log.error(this.getMessage());
    }

    @Override
    public String getMessage() {
        return "Pipeline with id: " + this.pipelineId + " not exists in system";
    }
}
