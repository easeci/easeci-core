package io.easeci.core.engine.runtime;

/**
 * Main entry-point interface to whole Pipeline running logic.
 * Instance of this class should be created every time when
 * pipeline is need to run. PipelineRunContext should gather and hold
 * whole context, information and required parameters of pipeline execution.
 * @author Karol Meksuła
 * 2021-04-01
 * */
interface PipelineRunContext {

    /**
     * Use this method to start pipeline execution.
     * */
    void run();
}
