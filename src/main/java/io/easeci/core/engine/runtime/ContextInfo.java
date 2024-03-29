package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.commons.PipelineState;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public abstract class ContextInfo {
    private UUID pipelineContextId;
    private PipelineState pipelineState;
}
