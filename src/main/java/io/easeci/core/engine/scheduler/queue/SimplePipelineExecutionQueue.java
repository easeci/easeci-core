package io.easeci.core.engine.scheduler.queue;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.engine.runtime.PipelineContextInfo;
import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimplePipelineExecutionQueue {

    private final PipelineExecutionQueue pipelineExecutionQueue;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ScheduledFuture<?> scheduledFuture;

    public SimplePipelineExecutionQueue(PipelineExecutionQueue pipelineExecutionQueue) {
        this.pipelineExecutionQueue = pipelineExecutionQueue;
        this.scheduledFuture = this.initializeQueueMonitor();
    }

    public boolean putPipelineOnQueue(PipelineContext pipelineContext) {
        boolean isPut = pipelineExecutionQueue.put(pipelineContext);
        pipelineContext.queued();
        return isPut;
    }

    private ScheduledFuture<?> initializeQueueMonitor() {
        int corePoolSize = LocationUtils.retrieveFromGeneralInt("schedule.thread-pool-execution", 1);
        int initialDelay = LocationUtils.retrieveFromGeneralInt("schedule.refresh-init-delay-seconds", 5);
        int period = LocationUtils.retrieveFromGeneralInt("schedule.refresh-interval-seconds", 5);
        if (Objects.isNull(scheduledThreadPoolExecutor)) {
            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
            return run(scheduledThreadPoolExecutor, initialDelay, period);
        }
        if (scheduledFuture.isCancelled()) {
            return run(scheduledThreadPoolExecutor, initialDelay, period);
        }
        return null;
    }

    private ScheduledFuture<?> run(ScheduledThreadPoolExecutor executor, int initialDelay, int period) {
        return executor.scheduleAtFixedRate(() -> this.pipelineExecutionQueue.next()
                .ifPresent(pipelineContext -> {
                    log.info("Pipeline execution before stored on queue now re-triggered automatically for pipelineContextId: {}", pipelineContext.getPipelineContextId());
                    PipelineContextInfo pipelineContextInfo = pipelineContext.prepareContextInfo();
                    pipelineContext.publish(pipelineContextInfo);
                }), initialDelay, period, TimeUnit.SECONDS);
    }
}
