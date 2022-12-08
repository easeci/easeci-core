package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;

/**
 * This interface defines the methods that are responsible for placing
 * the PipelineContext in the appropriate - idling Worker Node.
 * This interface is something like a load balancer in an EaseCI cluster.
 * The concept is inspired by the Kubernetes Scheduler.
 * @author Karol Meksuła
 * 2021-10-29
 * */
public interface PipelineScheduler {

    /**
     * @param pipelineContext is the PipelineContext ready for scheduling
     * @return ScheduleResponse holds details and information about scheduling process
     * */
    ScheduleResponse schedule(PipelineContext pipelineContext);
}
