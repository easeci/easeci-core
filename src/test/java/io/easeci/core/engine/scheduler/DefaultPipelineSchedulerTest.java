package io.easeci.core.engine.scheduler;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.node.connect.ClusterConnectionHub;
import io.easeci.core.workspace.WorkspaceInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultPipelineSchedulerTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly schedule pipeline")
    void shouldCorrectlySchedulePipeline() throws WorkspaceInitializationException {
        var hub = ClusterConnectionHub.getInstance();
        var scheduler = new DefaultPipelineScheduler(hub);


    }
}