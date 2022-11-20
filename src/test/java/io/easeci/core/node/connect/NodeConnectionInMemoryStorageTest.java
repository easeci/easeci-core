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
    void shouldFilterAndRejectDeadNodeConnections() throws WorkspaceInitializationException, NodeConnectionException {
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

        var allRetryableNodeConnections = storage.getAllAlive();

        assertEquals(2, allRetryableNodeConnections.size());
    }

    @Test
    @DisplayName("Should not allow to add node connection when nodeConnectionUuid just exists")
    void shouldNotAllowToAddNodeConnectionWhenNodeConnectionUuidJustExists() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var uuid = UUID.randomUUID();

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(uuid)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(uuid)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertThrows(NodeConnectionException.class, () -> storage.add(invalidNodeConnection)),
                () -> assertEquals(1, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should not allow to add node connection with the same nodeIp and nodePort")
    void shouldNotAllowToAddNodeConnectionWithTheSameNodeIpAndNodePort() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var nodeIp = "234.43.45.3";
        var nodePort = "8080";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeIp(nodeIp)
                .nodePort(nodePort)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeIp(nodeIp)
                .nodePort(nodePort)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertThrows(NodeConnectionException.class, () -> storage.add(invalidNodeConnection)),
                () -> assertEquals(1, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should allow to add node connection with the same nodeIp but another nodePort")
    void shouldAllowToAddNodeConnectionWithTheSameNodeIpButAnotherNodePort() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var nodeIp = "234.43.45.3";
        var nodePort = "8080";
        var anotherNodePort = "8081";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeIp(nodeIp)
                .nodePort(nodePort)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeIp(nodeIp)
                .nodePort(anotherNodePort)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertDoesNotThrow(() -> storage.add(invalidNodeConnection)),
                () -> assertEquals(2, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should not allow to add node connection when nodeName just exists")
    void shouldNotAllowToAddNodeConnectionWhenNodeNameJustExists() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var nodeName = "easeci-worker-01";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeName(nodeName)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeName(nodeName)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertThrows(NodeConnectionException.class, () -> storage.add(invalidNodeConnection)),
                () -> assertEquals(1, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should not allow to add node connection when domainName exists and nodePort is the same")
    void shouldNotAllowToAddNodeConnectionWhenDomainNameExistsAndNodePortIsTheSame() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var domainName = "easeci.pl";
        var nodePort = "8080";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodePort(nodePort)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodePort(nodePort)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertThrows(NodeConnectionException.class, () -> storage.add(invalidNodeConnection)),
                () -> assertEquals(1, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should allow to add node connection when domainName exists and nodePort is another")
    void shouldAllowToAddNodeConnectionWhenDomainNameExistsAndNodePortIsAnother() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var domainName = "easeci.pl";
        var nodePort = "8080";
        var anotherNodePort = "8081";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodePort(nodePort)
                .build();

        var invalidNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodePort(anotherNodePort)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertDoesNotThrow(() -> storage.add(invalidNodeConnection)),
                () -> assertEquals(2, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should allow to add node connection when domainName is blank and nodePort is same as exists with another blank domainName")
    void shouldAllowToAddNodeConnectionWhenDomainNameIsBlankAndNodePortIsSameAsExistsWithAnotherBlankDomainName() throws WorkspaceInitializationException, NodeConnectionException {
        var clusterConnectionIO = Mockito.mock(ClusterConnectionIO.class);
        var path = Paths.get("/tmp/settings.json");
        var storage = new NodeConnectionInMemoryStorage(clusterConnectionIO, path);
        var domainName = "";
        var firstIp = "43.34.23.192";
        var secondIp = "143.34.25.196";
        var nodePort = "8080";

        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodeIp(firstIp)
                .nodePort(nodePort)
                .build();

        var secondNodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName(domainName)
                .nodeIp(secondIp)
                .nodePort(nodePort)
                .build();

        storage.add(nodeConnection);

        assertAll(() -> assertDoesNotThrow(() -> storage.add(secondNodeConnection)),
                () -> assertEquals(2, storage.getAll().size()));
    }

    @Test
    @DisplayName("Should not throw exception when one of status is null")
    void shouldNotThrowExceptionWhenOneOfStatusIsNull() {
        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .domainName("easeci.pl")
                .nodePort("9000")
                .build();

        assertAll(() -> assertDoesNotThrow(nodeConnection::isReadyToWork),
                () -> assertFalse(nodeConnection::isReadyToWork));

    }

    @Test
    @DisplayName("Should return true when NodeConnectionState is ESTABLISHED and NodeProcessingState is IDLE")
    void shouldReturnTrueWhenIsIdleAndEstablished() {
        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeProcessingState(NodeProcessingState.IDLE)
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .domainName("easeci.pl")
                .nodePort("9000")
                .build();

        assertAll(() -> assertTrue(nodeConnection::isReadyToWork));
    }

    @Test
    @DisplayName("Should return false when NodeConnectionState is ESTABLISHED and NodeProcessingState is BUSY")
    void shouldReturnFalseWhenIsBusyAndEstablished() {
        var nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeProcessingState(NodeProcessingState.BUSY)
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .domainName("easeci.pl")
                .nodePort("9000")
                .build();

        assertAll(() -> assertFalse(nodeConnection::isReadyToWork));
    }
}