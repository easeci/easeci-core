package io.easeci.core.engine.runtime.commons;

import lombok.Data;

@Data
public class PipelineRuntimeDto {
    private PipelineRunStatus pipelineRunStatus;
    private String message;
}
