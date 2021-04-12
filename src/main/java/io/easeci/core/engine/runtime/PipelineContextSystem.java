package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
public class PipelineContextSystem implements PipelineRunEntryPoint, EventListener<PipelineContextInfo> {

    private static PipelineContextSystem system;

    private PipelineContextFactory factory;
    private List<PipelineContext> contextList;
    private PerformerTaskDistributor performerTaskDistributor;
    private VariableResolver variableResolver;
    private ScriptAssembler scriptAssembler;

    public static PipelineContextSystem getInstance() {
        if (isNull(system)) {
            system = new PipelineContextSystem();
        }
        return system;
    }

    private PipelineContextSystem() {
        this.contextList = new LinkedList<>();
        this.factory = new PipelineContextFactory();
        this.performerTaskDistributor = new StandardPerformerTaskDistributor();
        this.variableResolver = new StandardVariableResolver();
        this.scriptAssembler = new PythonScriptAssembler();
    }

    @Override
    public PipelineRunStatus runPipeline(UUID pipelineId) {
        log.info("Started to run pipeline with pipelineId: {}", pipelineId);

        final PipelineContext pipelineContext = this.factory.factorize(pipelineId, this,
                                                                        performerTaskDistributor, variableResolver, scriptAssembler);
        log.info("New PipelineContext created for pipelineId: {}", pipelineId);
        this.contextList.add(pipelineContext);
        pipelineContext.buildScript();

        return PipelineRunStatus.PIPELINE_EXEC_STARTED;
    }

    @Override
    public void receive(PipelineContextInfo event) {
        log.info("Event from PipelineContext received: {}", event.toString());
        // todo w tym momencie wiemy, że kontekst skończył budować skrypt i jest gotowy do odpalenia jako kontener
    }
}
