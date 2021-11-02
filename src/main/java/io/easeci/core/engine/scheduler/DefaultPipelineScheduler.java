package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultPipelineScheduler implements PipelineScheduler {

    @Override
    public ScheduleResult schedule(PipelineContext pipelineContext) {
        log.info("Scheduler instance received pipelineContext with pipelineContextId: {} for scheduling process", pipelineContext.getPipelineContextId());
        return null;
    }
}
