package io.easeci.core.node.connect;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static io.easeci.core.node.NodeUtils.readConnectToken;
import static java.util.Objects.isNull;

@Slf4j
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

    public static NodeConnection factorizeNodeConnection(NodeConnectionData nodeConnectionData) {
        boolean isTokenValid = connectTokenValid(nodeConnectionData.getConnectionToken());
        final Date dateNow = new Date();
        final NodeConnection nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(isTokenValid ? NodeConnectionState.REQUESTED : NodeConnectionState.UNAUTHORIZED)
                .lastConnectionStateChangeOccurred(dateNow)
                .connectionRequestOccurred(dateNow)
                .nodeIp(nodeConnectionData.getNodeIp())
                .nodeName(nodeConnectionData.getNodeName())
                .nodePort(nodeConnectionData.getNodePort())
                .domainName(nodeConnectionData.getDomainName())
                .transferProtocol(nodeConnectionData.getTransferProtocol())
                .build();
        if (isTokenValid) {
            addNodeConnection(nodeConnection);
        } else {
            log.info("Connection Token provided in request is not valid");
        }
        return nodeConnection;
    }

    private static boolean connectTokenValid(String requestedConnectionToken) {
        if (isNull(requestedConnectionToken)) {
            return false;
        }
        return readConnectToken()
                .map(requestedConnectionToken::equals)
                .orElseGet(() -> false);
    }

    private static synchronized void addNodeConnection(NodeConnection nodeConnection) {
        getInstance().nodeConnections.add(nodeConnection);
    }
}
