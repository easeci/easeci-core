package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    static PipelineContextSystem getInstance(PipelineContextFactory factory) {
        if (isNull(system)) {
            system = new PipelineContextSystem(factory);
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

    private PipelineContextSystem(PipelineContextFactory factory) {
        this.contextList = new LinkedList<>();
        this.factory = factory;
        this.performerTaskDistributor = new StandardPerformerTaskDistributor();
        this.variableResolver = new StandardVariableResolver();
        this.scriptAssembler = new PythonScriptAssembler();
    }

    @Override
    public PipelineRunStatus runPipeline(UUID pipelineId) {
        log.info("Started to run pipeline with pipelineId: {}", pipelineId);

        PipelineContext pipelineContext;
        try {
            pipelineContext = this.factory.factorize(
                    pipelineId, this,
                    performerTaskDistributor, variableResolver,
                    scriptAssembler
            );
        } catch (PipelineNotExists e) {
            log.error(e.getMessage());
            return PipelineRunStatus.PIPELINE_NOT_FOUND;
        }

        log.info("New PipelineContext created for pipelineId: {}", pipelineId);
        this.contextList.add(pipelineContext);
        pipelineContext.buildScript();

        return PipelineRunStatus.PIPELINE_EXEC_STARTED;
    }

    @Override
    public List<PipelineContextState> contextQueueState() {
        return this.contextList.stream()
                .map(PipelineContext::state)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void receive(PipelineContextInfo event) {
        log.info("Event from PipelineContext received: {}", event.toString());
        // todo w tym momencie wiemy, że kontekst skończył budować skrypt i jest gotowy do odpalenia jako kontener
    }

    static void destroyInstance() {
        system = null;
    }
}
