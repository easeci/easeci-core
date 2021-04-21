package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.assemble.PerformerTaskDistributor;
import io.easeci.core.engine.runtime.assemble.ScriptAssembler;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;

import java.util.UUID;

public class PipelineContextFactory {

    public PipelineContext factorize(UUID pipelineId,
                                     EventListener<PipelineContextInfo> eventListener,
                                     PerformerTaskDistributor performerTaskDistributor,
                                     GlobalVariablesFinder globalVariablesFinder,
                                     ScriptAssembler scriptAssembler) throws PipelineNotExists {
        return new PipelineContext(pipelineId, eventListener, performerTaskDistributor, globalVariablesFinder, scriptAssembler);
    }
}
