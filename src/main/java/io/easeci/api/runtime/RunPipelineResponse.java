package io.easeci.api.runtime;

import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class RunPipelineResponse {
    private PipelineRunStatus pipelineRunStatus;
    private String message;
    private Throwable exception;
}
