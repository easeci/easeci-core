package io.easeci.core.engine.scheduler;

import java.util.List;

public interface PipelineExecutionQueueSearcher {

    List<PipelineContextQueued> getAll();
}
