package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.vars.GlobalVariables;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class StandardPipelineRunContext implements PipelineRunContext {

    private final EasefileObjectModel easefileObjectModel;
    private boolean isRun = false;
    private PipelineExecutionStatus executionStatus;

    StandardPipelineRunContext(EasefileObjectModel easefileObjectModel) {
        this.easefileObjectModel = easefileObjectModel;
        this.executionStatus = PipelineExecutionStatus.PENDING;
    }

    @Override
    public void run() {
        log.info("Started execution of pipeline with metadata config: {}", this.easefileObjectModel.getMetadata().toString());
        this.isRun = true;
        this.executionStatus = PipelineExecutionStatus.STARTED;

        final PipelineRunner pipelineRunner = factorize();
        this.executionStatus = pipelineRunner.run();
    }

    private PipelineRunner factorize() {
        final GlobalVariables globalVariables = GlobalVariablesManager.getInstance();
        final PerformerTaskDistributor performerTaskDistributor = new AsyncPerformerTaskDistributor();
        return new PipelineRunner(this.easefileObjectModel, globalVariables, performerTaskDistributor);
    }
}
