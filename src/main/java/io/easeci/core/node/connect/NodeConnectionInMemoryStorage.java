package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.core.workspace.cluster.ClusterConnectionIO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.easeci.core.node.connect.NodeConnectionState.DEAD;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
class NodeConnectionInMemoryStorage {
    private Path clusterSettingsFileLocation;
    private List<NodeConnection> nodeConnections;
    private ClusterConnectionIO clusterConnectionIO;

    public NodeConnectionInMemoryStorage(ClusterConnectionIO clusterConnectionIO, Path clusterSettingsFileLocation) throws WorkspaceInitializationException {
        this.clusterSettingsFileLocation = clusterSettingsFileLocation;
        if (isNull(clusterConnectionIO)) {
            throw new WorkspaceInitializationException("NodeConnectionInMemoryStorage initialization exception");
        }
        this.clusterConnectionIO = clusterConnectionIO;
        log.info("Node connections storage initializing");
        if (Files.exists(clusterSettingsFileLocation)) {
            log.info("File for store cluster settings just exists here: {}", clusterSettingsFileLocation);
            nodeConnections = clusterConnectionIO.load(clusterSettingsFileLocation);
        } else {
            log.info("File for store cluster settings not exists here: {}, try to initialize", clusterSettingsFileLocation);
            this.nodeConnections = clusterConnectionIO.initialize(clusterSettingsFileLocation);
        }
    }

    public List<NodeConnection> getAll() {
        return nodeConnections;
    }

    public List<NodeConnection> getAllRefreshable() {
        return nodeConnections.stream()
                .filter(nodeConnection -> !DEAD.equals(nodeConnection.getNodeConnectionState()))
                .collect(Collectors.toList());
    }

    public NodeConnection add(NodeConnection nodeConnection) throws NodeConnectionException {
        validateNodeConnection(nodeConnection);
        nodeConnections.add(nodeConnection);
        log.info("Connection added: {}", nodeConnection.toString());
        try {
            clusterConnectionIO.save(clusterSettingsFileLocation, nodeConnections);
        } catch (IOException e) {
            log.error("Error occurred while save cluster file to: {}", clusterSettingsFileLocation, e);
        }
        return nodeConnection;
    }

    private void validateNodeConnection(NodeConnection nodeConnection) throws NodeConnectionException {
        if (nodeConnections.stream()
                .anyMatch(conn -> conn.getNodeConnectionUuid().equals(nodeConnection.getNodeConnectionUuid()))) {
            throw new NodeConnectionException("Connection with this nodeConnectionUuid just exists, uuid: " + nodeConnection.getNodeConnectionUuid());
        }
        if (nodeConnections.stream()
                .anyMatch(conn -> nonNull(conn.getNodeIp())
                        && nonNull(conn.getNodePort())
                        && conn.getNodeIp().equals(nodeConnection.getNodeIp())
                        && conn.getNodePort().equals(nodeConnection.getNodePort()))) {
            throw new NodeConnectionException("Connection with node with this nodeIp or nodePort just exists, uuid: " + nodeConnection.getNodeConnectionUuid());
        }
        if (nodeConnections.stream()
                .anyMatch(conn -> nonNull(conn.getNodeName()) && conn.getNodeName().equals(nodeConnection.getNodeName()))) {
            throw new NodeConnectionException("Connection with nodeName just exists, uuid: " + nodeConnection.getNodeConnectionUuid());
        }
        if (nodeConnections.stream()
                .anyMatch(conn -> nonNull(conn.getDomainName()) && nonNull(conn.getNodePort())
                        && conn.getDomainName().equals(nodeConnection.getDomainName()) &&
                        conn.getNodePort().equals(nodeConnection.getNodePort()))) {
            throw new NodeConnectionException("Connection with domainName and nodePort just exists, uuid: " + nodeConnection.getNodeConnectionUuid());
        }
    }

    public NodeConnection update(NodeConnection old, ConnectionStateResponse updatedStateResponse) {
        final int index = nodeConnections.indexOf(old);
        final NodeConnection nodeConnectionUpdated = old.mapNodeConnection(updatedStateResponse, determineRetryAttempts(old, updatedStateResponse));
        this.nodeConnections.set(index, nodeConnectionUpdated);
        try {
            clusterConnectionIO.save(clusterSettingsFileLocation, nodeConnections);
            log.info("Connection updated, now: {}", nodeConnectionUpdated.toString());
            return nodeConnectionUpdated;
        } catch (IOException e) {
            log.error("Error occurred while save cluster file to: {}", clusterSettingsFileLocation, e);
            return old;
        }
    }

    private int determineRetryAttempts(NodeConnection old, ConnectionStateResponse updatedStateResponse) {
        final int retryMaxAmount = LocationUtils.retrieveFromGeneralInt("cluster.worker-node.refresh-max-retry-attempts", 10);
        int retryCounter = old.getConnectionAttemptsCounter();
        boolean wasConnectionAttemptEndsWithError = wasConnectionAttemptEndsWithError(updatedStateResponse);
        if (wasConnectionAttemptEndsWithError) {
            if (++retryCounter >= retryMaxAmount) {
                log.info("Connection attempts {}/{} to worker node exceeded. Now worker node with uuid: {} is set as DEAD", retryCounter, retryMaxAmount, old.getNodeConnectionUuid());
                updatedStateResponse.setNodeConnectionState(DEAD);
            } else {
                log.info("Connection attempt {}/{} for worker node with uuid: {}", retryCounter, retryMaxAmount, old.getNodeConnectionUuid());
            }
        }
        return retryCounter;
    }

    private boolean wasConnectionAttemptEndsWithError(ConnectionStateResponse updatedStateResponse) {
        return NodeConnectionState.errorStatuses().contains(updatedStateResponse.getNodeConnectionState());
    }

    public boolean delete(UUID uuid) {
        boolean isRemoved = this.nodeConnections.stream()
                .filter(nodeConnection -> nodeConnection.getNodeConnectionUuid().equals(uuid))
                .findFirst()
                .map(nodeConnection -> this.nodeConnections.remove(nodeConnection))
                .orElse(false);
        if (isRemoved) {
            try {
                clusterConnectionIO.save(clusterSettingsFileLocation, nodeConnections);
            } catch (IOException e) {
                log.error("Error occurred while save cluster file", e);
                return false;
            }
        }
        return isRemoved;
    }
}
