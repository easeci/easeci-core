package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.commons.PipelineState;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class PipelineContextQueued {

    UUID pipelineContextId;
    UUID pipelineId;
    LocalDateTime contextCreatedDate;
    PipelineState pipelineState;
}
