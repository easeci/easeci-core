package io.easeci.core.engine.runtime.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
public class PipelineContextState {
    private final UUID pipelineContextId;
    private final UUID pipelineId;
    private final PipelineState pipelineState;
    private final long contextCreatedDate;
}
