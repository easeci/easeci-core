package io.easeci.core.engine.runtime.assemble;

import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StepsCollector {

    public List<PerformerCommand> collectSteps(List<Stage> stages) {
        final AtomicInteger stepOrder = new AtomicInteger(0);
        return stages.stream()
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
