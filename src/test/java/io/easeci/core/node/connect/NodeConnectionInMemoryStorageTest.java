package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.core.workspace.cluster.ClusterConnectionIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NodeConnectionInMemoryStorageTest {

    @Test
    @DisplayName("Should filter and reject DEAD node connections")
    void shouldFilterAndRejectDeadNodeConnections() throws WorkspaceInitializationException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);

        var node1 = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .build();
        var node2 = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .build();
        var node3 = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .build();

        storage.add(node1);
        storage.add(node2);
        storage.add(node3);

        // change state to DEAD after response from worker node
        var response = new ConnectionStateResponse();
        response.setNodeConnectionState(NodeConnectionState.DEAD);

        // update node connection
        storage.update(node3, response);

        var allRetryableNodeConnections = storage.getAllRetryable();

        assertEquals(2, allRetryableNodeConnections.size());
    }
}