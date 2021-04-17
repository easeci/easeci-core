package io.easeci.core.engine.runtime;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.runtime.assemble.PerformerTaskDistributor;
import io.easeci.core.engine.runtime.assemble.ScriptAssembler;
import io.easeci.core.engine.runtime.assemble.VariableResolver;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import io.easeci.core.engine.runtime.commons.PipelineState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_EXEC_STARTED;
import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PipelineContextSystemTest extends BaseWorkspaceContextTest {

    private final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
    private final UUID pipelineId = UUID.randomUUID();

    @Test
    @DisplayName("Should correctly run pipeline that exists in our projects")
    void successTest() {
        PipelineContextSystem.destroyInstance();
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);

        try {
            Mockito.when(pipelineContextFactory.factorize(any(UUID.class),
                                                          any(PipelineContextSystem.class),
                                                          any(PerformerTaskDistributor.class),
                                                          any(VariableResolver.class),
                                                          any(ScriptAssembler.class)))
                   .thenReturn(createPipelineContext(pipelineContextSystem));
        } catch (PipelineNotExists pipelineNotExists) {
            pipelineNotExists.printStackTrace();
        }

        PipelineRunStatus pipelineRunStatus = pipelineContextSystem.runPipeline(pipelineId);
        List<PipelineContextState> states = pipelineContextSystem.contextQueueState();

        assertAll(() -> assertEquals(PIPELINE_EXEC_STARTED, pipelineRunStatus),
                  () -> assertEquals(1, states.size()),
                  () -> assertEquals(this.pipelineId, states.get(0).getPipelineId()),
                  () -> assertEquals(PipelineState.NEW, states.get(0).getPipelineState()),
                  () -> assertNotNull(states.get(0).getPipelineContextId()));
    }

    private PipelineContext createPipelineContext(PipelineContextSystem system) throws PipelineNotExists {
        return new PipelineContext(this.pipelineId, system,
                    Mockito.mock(PerformerTaskDistributor.class),
                    Mockito.mock(VariableResolver.class),
                    Mockito.mock(ScriptAssembler.class)
                );
    }

    @Test
    @DisplayName("Should cannot find pipeline - not exists")
    void notFoundTest() {
        PipelineContextSystem.destroyInstance();
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        final UUID notExistingPipelineId = UUID.randomUUID();

        try {
            Mockito.when(pipelineContextFactory.factorize(any(UUID.class),
                                                          any(PipelineContextSystem.class),
                                                          any(PerformerTaskDistributor.class),
                                                          any(VariableResolver.class),
                                                          any(ScriptAssembler.class)))
                    .thenThrow(new PipelineNotExists(this.pipelineId));
        } catch (PipelineNotExists pipelineNotExists) {
            pipelineNotExists.printStackTrace();
        }

        PipelineRunStatus pipelineRunStatus = pipelineContextSystem.runPipeline(notExistingPipelineId);

        assertEquals(PIPELINE_NOT_FOUND, pipelineRunStatus);
    }
}