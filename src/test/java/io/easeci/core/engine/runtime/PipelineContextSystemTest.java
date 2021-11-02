package io.easeci.core.engine.runtime;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.engine.pipeline.Step;
import io.easeci.core.engine.runtime.assemble.*;
import io.easeci.core.engine.runtime.commons.PipelineContextState;
import io.easeci.core.engine.runtime.commons.PipelineRunStatus;
import io.easeci.core.engine.runtime.commons.PipelineState;
import io.easeci.core.engine.runtime.logs.LogBuffer;
import io.easeci.core.workspace.projects.PipelineIO;
import io.easeci.core.workspace.vars.GlobalVariablesFinder;
import io.easeci.core.workspace.vars.GlobalVariablesManager;
import io.easeci.extension.directive.CodeChunk;
import io.easeci.extension.directive.CodeLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_EXEC_STARTED;
import static io.easeci.core.engine.runtime.commons.PipelineRunStatus.PIPELINE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PipelineContextSystemTest extends BaseWorkspaceContextTest {

    private final PipelineContextFactory pipelineContextFactory = Mockito.mock(PipelineContextFactory.class);
    private final UUID pipelineId = UUID.randomUUID();

    @BeforeEach
    void setupBeforeEach() {
        PipelineContextSystem.destroyInstance();
    }

    @Test
    @DisplayName("Should correctly run pipeline that exists in our projects")
    void successTest() {
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        Mockito.when(pipelineIOMock.loadPipelineFile(pipelineId)).thenReturn(Optional.of(new EasefileObjectModel()));

        try {
            Mockito.when(pipelineContextFactory.factorize(any(UUID.class),
                                                          any(UUID.class),
                                                          any(PipelineContextSystem.class),
                                                          any(PerformerTaskDistributor.class),
                                                          any(GlobalVariablesFinder.class),
                                                          any(ScriptAssembler.class),
                                                          any(PipelineIO.class),
                                                          any(LogBuffer.class)))
                   .thenReturn(new PipelineContext(pipelineId, UUID.randomUUID(), pipelineContextSystem, null, null, null, pipelineIOMock, null));
        } catch (PipelineNotExists pipelineNotExists) {
            pipelineNotExists.printStackTrace();
        }

        PipelineRunStatus.PipelineRunStatusWrapper pipelineRunStatus = pipelineContextSystem.runPipeline(pipelineId);
        List<PipelineContextState> states = pipelineContextSystem.contextQueueState();

        assertAll(() -> assertEquals(PIPELINE_EXEC_STARTED, pipelineRunStatus.getPipelineRunStatus()),
                  () -> assertEquals(1, states.size()),
                  () -> assertEquals(this.pipelineId, states.get(0).getPipelineId()),
                  () -> assertEquals(PipelineState.NEW, states.get(0).getPipelineState()),
                  () -> assertNotNull(states.get(0).getPipelineContextId()));
    }

    @Test
    @DisplayName("Should cannot find pipeline - not exists")
    void notFoundTest() {
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);
        final UUID notExistingPipelineId = UUID.randomUUID();

        try {
            Mockito.when(pipelineContextFactory.factorize(any(UUID.class),
                                                          any(UUID.class),
                                                          any(PipelineContextSystem.class),
                                                          any(PerformerTaskDistributor.class),
                                                          any(GlobalVariablesFinder.class),
                                                          any(ScriptAssembler.class),
                                                          any(PipelineIO.class),
                                                          any(LogBuffer.class)))
                    .thenThrow(new PipelineNotExists(this.pipelineId));
        } catch (PipelineNotExists pipelineNotExists) {
            pipelineNotExists.printStackTrace();
        }

        PipelineRunStatus.PipelineRunStatusWrapper pipelineRunStatus = pipelineContextSystem.runPipeline(notExistingPipelineId);

        assertEquals(PIPELINE_NOT_FOUND, pipelineRunStatus.getPipelineRunStatus());
    }

    @Test
    @DisplayName("Should PipelineContext pass to Scheduler after validation. Mocked happy path")
    void passPipelineContextForScheduling() throws PipelineNotExists, InterruptedException {
        final PipelineContextSystem pipelineContextSystem = PipelineContextSystem.getInstance(pipelineContextFactory);

        UUID pipelineContextId = UUID.randomUUID();

        PipelineIO pipelineIOMock = Mockito.mock(PipelineIO.class);
        LogBuffer logBuffer = Mockito.mock(LogBuffer.class);
        GlobalVariablesFinder globalVariablesFinder = Mockito.mock(GlobalVariablesManager.class);
        PerformerTaskDistributor performerTaskDistributor = Mockito.mock(StandardPerformerTaskDistributor.class);
        ScriptAssembler scriptAssembler = new PythonScriptAssembler();

        PipelineContext pipelineContext = new PipelineContext(pipelineId, pipelineContextId, pipelineContextSystem, performerTaskDistributor, globalVariablesFinder, scriptAssembler, pipelineIOMock, logBuffer);

        EasefileObjectModel easefileObjectModel = EasefileObjectModel.builder()
                                                                     .stages(Collections.singletonList(Stage.builder()
                                                                             .name("Some test stage")
                                                                             .order(0)
                                                                             .variables(Collections.emptyList())
                                                                             .steps(Collections.singletonList(new Step(0, "bash", "echo 'Hello world!'")))
                                                                             .build()))
                                                                     .build();

        Mockito.when(pipelineIOMock.loadPipelineFile(pipelineId)).thenReturn(Optional.of(easefileObjectModel));
        Mockito.when(performerTaskDistributor.callPerformerSync(any()))
                .thenReturn(PerformerProduct.of(
                        CodeChunk.of(0, "bash", CodeLanguage.PYTHON_3, "#!/bin/bash\necho 'Hello world!'", "UTF-0"),
                        PerformerCommand.of(0, 0, 0, "", "")));

        Mockito.when(pipelineContextFactory.factorize(any(UUID.class),
                                                      any(UUID.class),
                                                      any(PipelineContextSystem.class),
                                                      any(PerformerTaskDistributor.class),
                                                      any(GlobalVariablesFinder.class),
                                                      any(ScriptAssembler.class),
                                                      any(PipelineIO.class),
                                                      any(LogBuffer.class)))
                .thenReturn(pipelineContext);

        pipelineContextSystem.runPipeline(pipelineId);

        Thread.sleep(1000); // we must wait for end of async flow

        assertEquals(PipelineState.READY_FOR_SCHEDULE, pipelineContext.getPipelineState());
    }
}