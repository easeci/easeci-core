package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.assemble.PerformerTaskDistributor;
import io.easeci.core.engine.runtime.assemble.ScriptAssembler;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;

import java.util.UUID;

public class PipelineContextFactory {

    public PipelineContext factorize(UUID pipelineId,
                                     UUID pipelineContextId,
                                     EventListener<ContextInfo> eventListener,
                                     PerformerTaskDistributor performerTaskDistributor,
                                     GlobalVariablesFinder globalVariablesFinder,
                                     ScriptAssembler scriptAssembler,
                                     PipelineIO pipelineIO,
                                     LogBuffer logBuffer) throws PipelineNotExists {
        return new PipelineContext(pipelineId, pipelineContextId, eventListener, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIO, logBuffer);
    }
}
