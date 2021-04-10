package io.easeci.core.engine.runtime;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.easeci.core.engine.runtime.Utils.provideEasefileObjectModel;
import static org.junit.jupiter.api.Assertions.*;

class PipelineRunnerTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly sort steps to execution in pipeline")
    void stepMappingAndSortingTest() {

        EasefileObjectModel eom = provideEasefileObjectModel();

        PipelineRunner pipelineRunner = new PipelineRunner(eom, null, null);

        List<PerformerCommand> performerCommands = pipelineRunner.collectSteps();

        assertAll(() -> assertEquals(14, performerCommands.size()),

                  () -> assertEquals(0, performerCommands.get(0).getOrder()),
                  () -> assertEquals(0, performerCommands.get(0).get_stageOrder()),
                  () -> assertEquals(0, performerCommands.get(0).get_stepOrder()),

                  () -> assertEquals(1, performerCommands.get(1).getOrder()),
                  () -> assertEquals(0, performerCommands.get(1).get_stageOrder()),
                  () -> assertEquals(1, performerCommands.get(1).get_stepOrder()),

                  () -> assertEquals(2, performerCommands.get(2).getOrder()),
                  () -> assertEquals(0, performerCommands.get(2).get_stageOrder()),
                  () -> assertEquals(2, performerCommands.get(2).get_stepOrder()),

                  () -> assertEquals(3, performerCommands.get(3).getOrder()),
                  () -> assertEquals(1, performerCommands.get(3).get_stageOrder()),
                  () -> assertEquals(0, performerCommands.get(3).get_stepOrder()),

                  () -> assertEquals(4, performerCommands.get(4).getOrder()),
                  () -> assertEquals(2, performerCommands.get(4).get_stageOrder()),
                  () -> assertEquals(0, performerCommands.get(4).get_stepOrder()),

                  () -> assertEquals(5, performerCommands.get(5).getOrder()),
                  () -> assertEquals(2, performerCommands.get(5).get_stageOrder()),
                  () -> assertEquals(1, performerCommands.get(5).get_stepOrder()),

                  () -> assertEquals(6, performerCommands.get(6).getOrder()),
                  () -> assertEquals(3, performerCommands.get(6).get_stageOrder()),
                  () -> assertEquals(0, performerCommands.get(6).get_stepOrder()),

                  () -> assertEquals(7, performerCommands.get(7).getOrder()),
                  () -> assertEquals(3, performerCommands.get(7).get_stageOrder()),
                  () -> assertEquals(1, performerCommands.get(7).get_stepOrder()),

                  () -> assertEquals(8, performerCommands.get(8).getOrder()),
                  () -> assertEquals(3, performerCommands.get(8).get_stageOrder()),
                  () -> assertEquals(2, performerCommands.get(8).get_stepOrder()),

                  () -> assertEquals(9, performerCommands.get(9).getOrder()),
                  () -> assertEquals(3, performerCommands.get(9).get_stageOrder()),
                  () -> assertEquals(3, performerCommands.get(9).get_stepOrder()),

                  () -> assertEquals(10, performerCommands.get(10).getOrder()),
                  () -> assertEquals(3, performerCommands.get(10).get_stageOrder()),
                  () -> assertEquals(4, performerCommands.get(10).get_stepOrder()),

                  () -> assertEquals(11, performerCommands.get(11).getOrder()),
                  () -> assertEquals(3, performerCommands.get(11).get_stageOrder()),
                  () -> assertEquals(5, performerCommands.get(11).get_stepOrder()),

                  () -> assertEquals(12, performerCommands.get(12).getOrder()),
                  () -> assertEquals(3, performerCommands.get(12).get_stageOrder()),
                  () -> assertEquals(6, performerCommands.get(12).get_stepOrder()),

                  () -> assertEquals(13, performerCommands.get(13).getOrder()),
                  () -> assertEquals(3, performerCommands.get(13).get_stageOrder()),
                  () -> assertEquals(7, performerCommands.get(13).get_stepOrder())
        );
    }
}