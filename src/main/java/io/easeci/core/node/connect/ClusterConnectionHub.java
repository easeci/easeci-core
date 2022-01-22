package io.easeci.core.node.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

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

    protected synchronized void addNodeConnection(NodeConnection nodeConnection) {
        ClusterConnectionHub.getInstance().nodeConnections.add(nodeConnection);
        CompletableFuture.supplyAsync(() -> clusterConnectionStateMonitor)
                .thenAccept(monitor -> {
                    ClusterConnectionStateMonitor.ConnectionStateRequest connectionStateRequest = prepareNodeConnectionState(nodeConnection);
                    NodeConnectionState nodeConnectionStateUpdated = monitor.checkWorkerState(connectionStateRequest);
                    int index = nodeConnections.indexOf(nodeConnection);
                    this.nodeConnections.set(index, nodeConnection.recreate(nodeConnectionStateUpdated));
                });
    }

    private ClusterConnectionStateMonitor.ConnectionStateRequest prepareNodeConnectionState(NodeConnection nodeConnection) {
        return ClusterConnectionStateMonitor.ConnectionStateRequest.of(nodeConnection.getNodeIp(),
                nodeConnection.getNodePort(),
                nodeConnection.getDomainName(),
                nodeConnection.getNodeName(),
                nodeConnection.getTransferProtocol());
    }

}
