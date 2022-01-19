package io.easeci.core.node.connect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public class ClusterConnectionFactory {
    private static ClusterConnectionFactory instance;
    private List<NodeConnection> nodeConnections;

    private ClusterConnectionFactory() {
        this.nodeConnections = new ArrayList<>();
    }

    public static synchronized ClusterConnectionFactory getInstance() {
        if (isNull(instance)) {
            instance = new ClusterConnectionFactory();
        }
        return instance;
    }

    private static synchronized void addNodeConnection(NodeConnection nodeConnection) {
        getInstance().nodeConnections.add(nodeConnection);
    }

    public static NodeConnection factorizeNodeConnection(NodeConnectionData nodeConnectionData) {
        final Date dateNow = new Date();
        final NodeConnection nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(NodeConnectionState.REQUESTED)
                .lastConnectionStateChangeOccurred(dateNow)
                .connectionRequestOccurred(dateNow)
                .nodeIp(nodeConnectionData.getNodeIp())
                .nodeName(nodeConnectionData.getNodeName())
                .nodePort(nodeConnectionData.getNodePort())
                .domainName(nodeConnectionData.getDomainName())
                .transferProtocol(nodeConnectionData.getTransferProtocol())
                .build();
        addNodeConnection(nodeConnection);
        return nodeConnection;
    }

    private void connectTokenValid() {

    }
}
