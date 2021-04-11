package io.easeci.core.engine.runtime;

import java.util.UUID;

public class PipelineContextFactory {

    public PipelineContext factorize(UUID pipelineId, EventListener<PipelineContextInfo> eventListener) {
        return new PipelineContext(eventListener);
    }
}
