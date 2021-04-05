package io.easeci.core.engine.runtime;

import lombok.Data;

@Data
public class PipelineRuntimeDto {
    private PipelineExecutionStatus pipelineExecutionStatus;
    private String message;
}
