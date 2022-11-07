package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.core.workspace.cluster.ClusterConnectionIO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.easeci.core.workspace.LocationUtils.getClusterSettingsFileLocation;
import static java.util.Objects.isNull;

@Slf4j
class NodeConnectionInMemoryStorage {
    private static final Path clusterSettingsFileLocation = getClusterSettingsFileLocation();
    private List<NodeConnection> nodeConnections;
    private ClusterConnectionIO clusterConnectionIO;

    public NodeConnectionInMemoryStorage(ClusterConnectionIO clusterConnectionIO) throws WorkspaceInitializationException {
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
            clusterConnectionIO.initialize(clusterSettingsFileLocation);
        }
    }

    public List<NodeConnection> getAll() {
        return nodeConnections;
    }

    public NodeConnection add(NodeConnection nodeConnection) {
        nodeConnections.add(nodeConnection);
        log.info("Connection added: {}", nodeConnection.toString());
        try {
            clusterConnectionIO.save(clusterSettingsFileLocation, nodeConnections);
        } catch (IOException e) {
            log.error("Error occurred while save cluster file to: {}", clusterSettingsFileLocation, e);
        }
        return nodeConnection;
    }

    public NodeConnection update(NodeConnection old, ConnectionStateResponse updatedStateResponse) {
        final int index = nodeConnections.indexOf(old);
        final NodeConnection nodeConnectionUpdated = old.mapNodeConnection(updatedStateResponse);
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
}
