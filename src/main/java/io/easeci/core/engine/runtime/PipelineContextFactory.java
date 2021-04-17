package io.easeci.core.engine.runtime;

import io.easeci.core.engine.runtime.assemble.PerformerTaskDistributor;
import io.easeci.core.engine.runtime.assemble.ScriptAssembler;
import io.easeci.core.engine.runtime.assemble.VariableResolver;

import java.util.UUID;

public class PipelineContextFactory {

    public PipelineContext factorize(UUID pipelineId,
                                     EventListener<PipelineContextInfo> eventListener,
                                     PerformerTaskDistributor performerTaskDistributor,
                                     VariableResolver variableResolver,
                                     ScriptAssembler scriptAssembler) throws PipelineNotExists {
        return new PipelineContext(pipelineId, eventListener, performerTaskDistributor, variableResolver, scriptAssembler);
    }
}
