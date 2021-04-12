package io.easeci.core.engine.runtime;

import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.extension.directive.CodeChunk;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class PipelineContext implements PipelineRunnable, PipelineScriptBuilder, EventPublisher<PipelineContextInfo> {

    private final UUID pipelineId;
    private final EventListener<PipelineContextInfo> eventListener;
    private final PerformerTaskDistributor performerTaskDistributor;
    private final VariableResolver variableResolver;
    private final ScriptAssembler scriptAssembler;

    private EasefileObjectModel eom;
    private String scriptAssembled;

    public PipelineContext(UUID pipelineId,
                           EventListener<PipelineContextInfo> eventListener,
                           PerformerTaskDistributor performerTaskDistributor,
                           VariableResolver variableResolver,
                           ScriptAssembler scriptAssembler) {
        this.pipelineId = pipelineId;
        this.eventListener = eventListener;
        this.performerTaskDistributor = performerTaskDistributor;
        this.variableResolver = variableResolver;
        this.scriptAssembler = scriptAssembler;
    }

    @Override
    public PipelineContextInfo runPipeline() {
        return null;
    }

    @Override
    public void buildScript() {
        CompletableFuture.runAsync(() -> {
            log.info("Starting collecting script chunks and waiting for all Performers to end these jobs");

            // load file from file
            this.eom = loadFromFile(this.pipelineId);
            // resolve variables
            this.eom = this.variableResolver.resolve(this.eom);

            // call performer for each directive
            StepsCollector stepsCollector = new StepsCollector();
            final List<PerformerCommand> performerCommands = stepsCollector.collectSteps(this.eom.getStages());
            final List<PerformerProduct> performerProducts = new ArrayList<>(performerCommands.size());
            performerCommands.stream()
                    .map(this.performerTaskDistributor::callPerformer)
                    .forEach(future -> future.thenApply(performerProduct -> {
                        performerProducts.add(performerProduct);
                        return performerProduct;
                    }));

            // merge-assemble code chunks to full script
            List<CodeChunk> codeChunks = performerProducts.stream()
                    .map(PerformerProduct::getCodeChunk)
                    .collect(Collectors.toList());
            this.scriptAssembled = this.scriptAssembler.assemble(codeChunks);

            log.info("Script chunks collecting finished");
            PipelineContextInfo info = new PipelineContextInfo();
            info.setPipelineContextId(UUID.randomUUID());
            this.publish(info);
        });
    }

    @Override
    public void publish(PipelineContextInfo event) {
        log.info("Event from PipelineContext published to EventListener: {}", event.toString());
        this.eventListener.receive(event);
    }

    private EasefileObjectModel loadFromFile(UUID pipelineId) {
        return null;
    }

    private Path saveScript(String fullScript) {
        // zapisz skrypt do pliku
        // zapisz sumę kontrolną -> przemyśl i stwórz odpowiedni mechanizm
        return null;
    }

}
