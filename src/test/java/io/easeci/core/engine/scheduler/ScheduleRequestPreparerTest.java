package io.easeci.core.engine.scheduler;

import io.easeci.core.node.connect.ClusterInformation;
import io.easeci.server.TransferProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleRequestPreparerTest {

    @Test
    @DisplayName("Should correctly build master node url")
    void shouldCorrectlyBuildMasterNodeUrl() {
        var clusterInfo = Mockito.mock(ClusterInformation.class);
        Mockito.when(clusterInfo.transferProtocol()).thenReturn(TransferProtocol.HTTPS);
        Mockito.when(clusterInfo.domainName()).thenReturn("master-01.easeci.pl");
        Mockito.when(clusterInfo.apiVersionPrefix()).thenReturn("api/v1");

        var scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInfo);

        URL url = scheduleRequestPreparer.buildMasterUrl();

        assertEquals("https://master-01.easeci.pl/api/v1/pipeline/execution", url.toString());
    }

    @Test
    @DisplayName("Should correctly build master node url even when slashes are redundant at end of string")
    void shouldCorrectlyBuildMasterNodeUrlEvenWhenSlashesAreRedundantAtEndOfString() {
        var clusterInfo = Mockito.mock(ClusterInformation.class);
        Mockito.when(clusterInfo.transferProtocol()).thenReturn(TransferProtocol.HTTPS);
        Mockito.when(clusterInfo.domainName()).thenReturn("//master-01.easeci.pl///");
        Mockito.when(clusterInfo.apiVersionPrefix()).thenReturn("/api/v1//");

        var scheduleRequestPreparer = new ScheduleRequestPreparer(clusterInfo);

        URL url = scheduleRequestPreparer.buildMasterUrl();

        assertEquals("https://master-01.easeci.pl/api/v1/pipeline/execution", url.toString());
    }
}