package io.easeci.core.node.connect;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.server.TransferProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static io.easeci.core.node.connect.NodeConnectionState.DEAD;
import static org.junit.jupiter.api.Assertions.*;

class ClusterConnectionHubTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should mark node connection as DEAD when retries attempts exceeded")
    void shouldMarkNodeConnectionAsDEADWhenRetriesAttemptsExceeded() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionHub = ClusterConnectionHub.getInstance();

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .connectionRequestOccurred(new Date())
                .nodeIp("127.0.0.1")
                .nodePort("9001")
                .domainName("localhost")
                .nodeName("local-worker-node-01")
                .transferProtocol(TransferProtocol.HTTP)
                .connectionAttemptsCounter(0)
                .build();

        clusterConnectionHub.tryAddNodeConnection(nodeConnection);
        int clusterNodeSize = clusterConnectionHub.getClusterDetails().getClusterNodes().size();

        assertAll(() -> assertEquals(1, clusterNodeSize),
                () -> assertNotEquals(DEAD, clusterConnectionHub.getClusterDetails().getClusterNodes().get(0).getNodeConnectionState()));

        int maxAttemptsBeforeDeath = LocationUtils.retrieveFromGeneralInt("cluster.worker-node.refresh-max-retry-attempts", 10);

        for (int i = 0; i < maxAttemptsBeforeDeath; i++) {
            clusterConnectionHub.invokeRequestNodeForConnectionState();
        }

        assertAll(() -> assertEquals(1, clusterNodeSize),
                () -> assertEquals(DEAD, clusterConnectionHub.getClusterDetails().getClusterNodes().get(0).getNodeConnectionState()));
    }
}