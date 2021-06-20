package io.easeci.core.engine.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PipelineContextHistoryEntity {
    private String pipelineId;
    private String pipelineContextId;
    private LocalDateTime runPipelineDateTime;

    public byte[] toEntity() {
        return (pipelineId + "," + pipelineContextId + "," + runPipelineDateTime.toString() + "\n").getBytes(StandardCharsets.UTF_8);
    }
}
