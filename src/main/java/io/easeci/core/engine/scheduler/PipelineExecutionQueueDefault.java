package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public class PipelineExecutionQueueDefault implements PipelineExecutionQueue, PipelineExecutionQueueSearcher {

    private final Queue<PipelineContext> pipelineContextQueue;

    public PipelineExecutionQueueDefault() {
        this.pipelineContextQueue = new LinkedList<>();
    }

    @Override
    public boolean put(PipelineContext pipelineContext) {
        return pipelineContextQueue.add(pipelineContext);
    }

    @Override
    public Optional<PipelineContext> next() {
        return Optional.ofNullable(pipelineContextQueue.poll());
    }

    @Override
    public boolean isEmpty() {
        return pipelineContextQueue.isEmpty();
    }

    @Override
    public List<PipelineContextQueued> getAll() {
        return pipelineContextQueue.stream()
                .map(pipelineContext -> PipelineContextQueued.builder()
                        .pipelineContextId(pipelineContext.getPipelineContextId())
                        .pipelineId(pipelineContext.getPipelineId())
                        .contextCreatedDate(pipelineContext.getContextCreatedDate())
                        .pipelineState(pipelineContext.getPipelineState())
                        .build())
                .collect(Collectors.toList());
    }
}
