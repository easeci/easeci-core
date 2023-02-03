package io.easeci.core.engine.scheduler.queue;

import java.util.List;

public interface PipelineExecutionQueueSearcher {

    List<PipelineContextQueued> getAll();
}
