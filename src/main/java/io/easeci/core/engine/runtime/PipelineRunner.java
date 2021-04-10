package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.workspace.vars.GlobalVariables;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
public class PipelineRunner {

    private final EasefileObjectModel eom;
    private final GlobalVariables globalVariables;
    private final PerformerTaskDistributor performerTaskDistributor;

    public PipelineRunner(EasefileObjectModel eom, GlobalVariables globalVariables, PerformerTaskDistributor performerTaskDistributor) {
        this.eom = eom;
        this.globalVariables = globalVariables;
        this.performerTaskDistributor = performerTaskDistributor;
    }

    public PipelineExecutionStatus run() {
        if (isNull(this.eom)) {
            log.error("Execution of pipeline failed because EasefileObjectModel is null");
            return PipelineExecutionStatus.FAILURE;
        }
        this.resolveVariables();
        this.resolveSecrets();
        this.distributeTasks();
        return PipelineExecutionStatus.PROCESSING;
    }

    protected void resolveSecrets() {

    }

    protected void resolveVariables() {

    }

    protected void distributeTasks() {

    }

    List<PerformerCommand> collectSteps() {
        final AtomicInteger stepOrder = new AtomicInteger(0);
        return eom.getStages().stream()
                  .sorted(Comparator.comparingInt(Stage::getOrder))
                  .flatMap(stage -> stage.getSteps().stream()
                          .sorted(Comparator.comparingInt(Step::getOrder))
                          .map(step -> PerformerCommand.of(stage.getOrder(),
                                                           step.getOrder(),
                                                           stepOrder.getAndIncrement(),
                                                           step.getDirectiveName(),
                                                           step.getInvocationBody())))
                  .collect(Collectors.toList());
    }
}

@Data
@ToString
@AllArgsConstructor(staticName = "of")
class PerformerCommand {
    // for information purposes only
    private int _stageOrder;
    private int _stepOrder;

    // the parameter determines the order of execution in pipeline flow
    private int order;
    private String directiveName;
    private String invocationBody;
}
