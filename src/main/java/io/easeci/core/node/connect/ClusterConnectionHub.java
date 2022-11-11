package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ClusterDetailsResponse;
import io.easeci.core.node.connect.dto.ClusterNodeDetails;
import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.core.workspace.WorkspaceInitializationException;
import io.easeci.core.workspace.cluster.ClusterConnectionIO;
import io.easeci.core.workspace.cluster.DefaultClusterConnectionIO;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.easeci.core.workspace.LocationUtils.getClusterSettingsFileLocation;
import static java.util.Objects.isNull;

@Slf4j
public class ClusterConnectionHub {
    private static ClusterConnectionHub instance;
    private NodeConnectionInMemoryStorage nodeConnectionInMemoryStorage;
    private NodeConnector nodeConnector;
    private ClusterConnectionStateMonitor clusterConnectionStateMonitor;
    private ScheduledFuture<?> nodeConnectionScheduledFuture;

    private ClusterConnectionHub() throws WorkspaceInitializationException {
        ClusterConnectionIO clusterConnectionIO = new DefaultClusterConnectionIO();
        this.nodeConnectionInMemoryStorage = new NodeConnectionInMemoryStorage(clusterConnectionIO, getClusterSettingsFileLocation());
        this.nodeConnector = new NodeConnector();
        this.clusterConnectionStateMonitor = new ClusterConnectionStateMonitor(nodeConnector);
        this.nodeConnectionScheduledFuture = nodeConnectionMonitor();
    }

    public static synchronized ClusterConnectionHub getInstance() throws WorkspaceInitializationException {
        if (isNull(instance)) {
            instance = new ClusterConnectionHub();
        }
        return instance;
    }

    public ClusterDetailsResponse getClusterDetails() {
        return ClusterDetailsResponse.of(nodeConnectionInMemoryStorage.getAll().stream()
                .map(nodeConnection -> ClusterNodeDetails.builder()
                        .nodeConnectionUuid(nodeConnection.getNodeConnectionUuid())
                        .nodeConnectionState(nodeConnection.getNodeConnectionState())
                        .nodeProcessingState(nodeConnection.getNodeProcessingState())
                        .connectionRequestOccurred(nodeConnection.getConnectionRequestOccurred())
                        .lastConnectionStateChangeOccurred(nodeConnection.getLastConnectionStateChangeOccurred())
                        .nodeIp(nodeConnection.getNodeIp())
                        .nodePort(nodeConnection.getNodePort())
                        .domainName(nodeConnection.getDomainName())
                        .nodeName(nodeConnection.getNodeName())
                        .transferProtocol(nodeConnection.getTransferProtocol())
                        .build())
                .collect(Collectors.toList()));
    }

    protected synchronized void tryAddNodeConnection(NodeConnection nodeConnection) throws NodeConnectionException {
        nodeConnectionInMemoryStorage.add(nodeConnection);
        CompletableFuture.runAsync(() -> requestNodeForConnectionState(nodeConnection));
    }

    private void requestNodeForConnectionState(NodeConnection nodeConnection) {
        ConnectionStateRequest connectionStateRequest = prepareNodeConnectionState(nodeConnection);
        ConnectionStateResponse nodeConnectionStateUpdated = clusterConnectionStateMonitor.checkWorkerState(connectionStateRequest);
        log.info("Status of worker node obtained: {}, we can update state of this one connection", nodeConnectionStateUpdated.getNodeConnectionState());
        this.nodeConnectionInMemoryStorage.update(nodeConnection, nodeConnectionStateUpdated);
    }

    private ConnectionStateRequest prepareNodeConnectionState(NodeConnection nodeConnection) {
        return ConnectionStateRequest.of(nodeConnection.getNodeIp(),
                nodeConnection.getNodePort(),
                nodeConnection.getDomainName(),
                nodeConnection.getNodeName(),
                nodeConnection.getTransferProtocol());
    }

    private ScheduledFuture<?> nodeConnectionMonitor() {
        int corePoolSize = LocationUtils.retrieveFromGeneralInt("cluster.worker-node.thread-pool-execution", 1);
        int initialDelay = LocationUtils.retrieveFromGeneralInt("cluster.worker-node.refresh-init-delay-seconds", 0);
        int period = LocationUtils.retrieveFromGeneralInt("cluster.worker-node.refresh-interval-seconds", 5);
        final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(this::invokeRequestNodeForConnectionState, initialDelay, period, TimeUnit.SECONDS);
    }

    protected void invokeRequestNodeForConnectionState() {
        this.nodeConnectionInMemoryStorage.getAllRetryable()
                .forEach(this::requestNodeForConnectionState);
    }

    public boolean delete(String nodeConnectionUuid) throws IllegalArgumentException {
        UUID uuid = UUID.fromString(nodeConnectionUuid);
        return this.nodeConnectionInMemoryStorage.delete(uuid);
    }
}
