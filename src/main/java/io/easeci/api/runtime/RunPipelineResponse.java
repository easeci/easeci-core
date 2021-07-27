package io.easeci.api.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunPipelineResponse {
    private PipelineRunStatus pipelineRunStatus;
    private UUID pipelineContextId;
    private String message;
    private Throwable exception;

    private RunPipelineResponse() {}

    public static RunPipelineResponse of(PipelineRunStatus.PipelineRunStatusWrapper pipelineRunStatusWrapper, String message, Throwable exception) {
        RunPipelineResponse response = new RunPipelineResponse();
        response.pipelineRunStatus = pipelineRunStatusWrapper.getPipelineRunStatus();
        response.pipelineContextId = pipelineRunStatusWrapper.getPipelineContextId();
        response.message = message;
        response.exception = exception;
        return response;
    }
}
