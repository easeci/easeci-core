package io.easeci.core.engine.scheduler;

import io.easeci.core.engine.runtime.PipelineContext;
import io.easeci.core.engine.runtime.PipelineNotExists;
import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.server.CommunicationType;
import io.easeci.server.TransferProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleRequestPreparerTest {

    @Test
    @DisplayName("Should correctly build Urls object for log publishing via websocket and http protocol")
    void shouldCorrectlyBuildMasterNodeUrl() throws PipelineNotExists {
        var clusterInfo = Mockito.mock(ClusterInformation.class);
        Mockito.when(clusterInfo.transferProtocol()).thenReturn(TransferProtocol.HTTPS);
        Mockito.when(clusterInfo.domainName()).thenReturn("master-01.easeci.pl");
        Mockito.when(clusterInfo.apiVersionPrefix()).thenReturn("api/v1");
        Mockito.when(clusterInfo.communicationType()).thenReturn(CommunicationType.DOMAIN);

        var scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInfo);
        var pipelineContext = Mockito.mock(PipelineContext.class);
        Mockito.when(pipelineContext.getPipelineContextId()).thenReturn(UUID.randomUUID());
        Mockito.when(pipelineContext.getExecutableScript()).thenReturn("");

        var scheduleRequest = scheduleRequestPreparer.prepareRequest(pipelineContext);

        assertAll(() -> assertEquals("https://master-01.easeci.pl/api/v1/pipeline/logs/http", scheduleRequest.getMetadata().getUrls().httpLogUrl()),
                () -> assertEquals("ws://master-01.easeci.pl/api/v1/pipeline/logs/ws", scheduleRequest.getMetadata().getUrls().wsLogUrl()));
    }

    @Test
    @DisplayName("Should correctly build master node url even when slashes are redundant at end of string")
    void shouldCorrectlyBuildMasterNodeUrlEvenWhenSlashesAreRedundantAtEndOfString() {
        var clusterInfo = Mockito.mock(ClusterInformation.class);
        Mockito.when(clusterInfo.transferProtocol()).thenReturn(TransferProtocol.HTTPS);
        Mockito.when(clusterInfo.domainName()).thenReturn("//master-01.easeci.pl///");
        Mockito.when(clusterInfo.apiVersionPrefix()).thenReturn("/api/v1//");
        Mockito.when(clusterInfo.communicationType()).thenReturn(CommunicationType.DOMAIN);

        var scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInfo);
        var pipelineContext = Mockito.mock(PipelineContext.class);
        Mockito.when(pipelineContext.getPipelineContextId()).thenReturn(UUID.randomUUID());
        Mockito.when(pipelineContext.getExecutableScript()).thenReturn("");

        var scheduleRequest = scheduleRequestPreparer.prepareRequest(pipelineContext);

        assertAll(() -> assertEquals("https://master-01.easeci.pl/api/v1/pipeline/logs/http", scheduleRequest.getMetadata().getUrls().httpLogUrl()),
                () -> assertEquals("ws://master-01.easeci.pl/api/v1/pipeline/logs/ws", scheduleRequest.getMetadata().getUrls().wsLogUrl()));
    }
}