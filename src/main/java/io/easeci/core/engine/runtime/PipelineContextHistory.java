package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.logs.LogRail;

import java.util.UUID;

public interface PipelineContextHistory {

    boolean saveHistoricalLogEntity(UUID pipelineContextId, UUID pipelineId);

    LogRail findHistoricalLogs(UUID pipelineContextId);
}
