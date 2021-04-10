package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.projects.ProjectManager;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.easeci.core.engine.runtime.PipelineRunStatus.PIPELINE_EXEC_QUEUED;
import static io.easeci.core.engine.runtime.PipelineRunStatus.PIPELINE_NOT_FOUND;

@Slf4j
public class ScheduledExecutionQueue implements ExecutionQueue {

    private PipelineIO pipelineIO;
    private Queue<EasefileObjectModel> waitingEomQueue;
    private Queue<PipelineRunContext> justExecQueue;
    private ScheduledExecutorService executorService;

    private static ExecutionQueue executionQueue;

    public static ExecutionQueue getInstance() {
        if (executionQueue == null) {
            ScheduledExecutionQueue.executionQueue = new ScheduledExecutionQueue(ProjectManager.getInstance());
        }
        return executionQueue;
    }

    private ScheduledExecutionQueue(PipelineIO pipelineIO) {
        this.pipelineIO = pipelineIO;
        this.justExecQueue = new LinkedList<>();
        this.waitingEomQueue = new LinkedList<>();
        this.executorService = new ScheduledThreadPoolExecutor(1); // todo load this from general.yml
        this.executorService.scheduleAtFixedRate(this::execNext, 15, 60, TimeUnit.SECONDS); // todo as above, after refactor yaml injecting
    }

    @Override
    public PipelineRunStatus runPipeline(UUID pipelineId) {
        return pipelineIO.loadPipelineFile(pipelineId)
                         .map(this::put)
                         .orElse(PIPELINE_NOT_FOUND);
    }

    private PipelineRunStatus put(EasefileObjectModel eom) {
        log.info("Pipeline was put on waitingEomQueue, now on queue is waiting {} EasefileObjectModel for execution", waitingEomQueue.size());
        waitingEomQueue.add(eom);
        return PIPELINE_EXEC_QUEUED;
    }

    private void execNext() {
        if (shouldRunNext()) {
            final EasefileObjectModel eom = waitingEomQueue.poll();
            final PipelineRunContext ctx = new StandardPipelineRunContext(eom);
            justExecQueue.add(ctx);
            ctx.run();
        } else {
            log.info("There are no task to execution for pipeline scheduler");
        }
    }

    private boolean shouldRunNext() {
        if (this.waitingEomQueue.isEmpty()) {
            log.info("waitingEomQueue is empty - no task to execution");
            return false;
        }
        // todo check memory, cpu and how many tasks are running now
        return true;
    }
}
