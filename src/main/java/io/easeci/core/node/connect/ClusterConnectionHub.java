package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

@Slf4j
public class ClusterConnectionHub {
    private static ClusterConnectionHub instance;
    private List<NodeConnection> nodeConnections;
    private NodeConnector nodeConnector;
    private ClusterConnectionStateMonitor clusterConnectionStateMonitor;

    private ClusterConnectionHub() {
        this.nodeConnections = new ArrayList<>();
        this.nodeConnector = new NodeConnector();
        this.clusterConnectionStateMonitor = new ClusterConnectionStateMonitor(nodeConnector);
    }

    public static synchronized ClusterConnectionHub getInstance() {
        if (isNull(instance)) {
            instance = new ClusterConnectionHub();
        }
        return instance;
    }

    protected synchronized void tryAddNodeConnection(NodeConnection nodeConnection) {
        ClusterConnectionHub.getInstance().nodeConnections.add(nodeConnection);
        CompletableFuture.supplyAsync(() -> clusterConnectionStateMonitor)
                .thenAccept(connectionStateMonitor -> {
                    ConnectionStateRequest connectionStateRequest = prepareNodeConnectionState(nodeConnection);
                    ConnectionStateResponse nodeConnectionStateUpdated = connectionStateMonitor.checkWorkerState(connectionStateRequest);
                    log.info("Status of worker node obtained, we can update state of this one connection");
                    this.update(nodeConnection, nodeConnectionStateUpdated);
                });
    }

    protected void update(NodeConnection old, ConnectionStateResponse updatedStateResponse) {
        final int index = nodeConnections.indexOf(old);
        final NodeConnection nodeConnectionUpdated = old.recreate(updatedStateResponse);
        this.nodeConnections.set(index, old.recreate(updatedStateResponse));
        log.info("Connection updated, now: {}", nodeConnectionUpdated.toString());
    }

    private ConnectionStateRequest prepareNodeConnectionState(NodeConnection nodeConnection) {
        return ConnectionStateRequest.of(nodeConnection.getNodeIp(),
                nodeConnection.getNodePort(),
                nodeConnection.getDomainName(),
                nodeConnection.getNodeName(),
                nodeConnection.getTransferProtocol());
    }

}
