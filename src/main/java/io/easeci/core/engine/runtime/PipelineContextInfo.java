package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.commons.PipelineState;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Data
@ToString
public class PipelineContextInfo {
    private UUID pipelineContextId;
    private PipelineState pipelineState;
    private Date creationDate;
    private Date finishDate;
}
