package io.easeci.core.node.connect;

import io.easeci.core.workspace.WorkspaceInitializationException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
public class ClusterConnectionFactory {

    private ClusterInformation clusterInformation;

    public ClusterConnectionFactory() {
        this.clusterInformation = new ClusterInformationDefault();
    }

    public ClusterConnectionFactory(ClusterInformation clusterInformation) {
        this.clusterInformation = clusterInformation;
    }

    public NodeConnection factorizeNodeConnection(NodeConnectionData nodeConnectionData) throws WorkspaceInitializationException, NodeConnectionException {
        boolean isTokenValid = validateConnectionToken(nodeConnectionData.getConnectionToken());
        final Date dateNow = new Date();
        final NodeConnection nodeConnection = NodeConnection.builder()
                .nodeConnectionUuid(UUID.randomUUID())
                .nodeConnectionState(isTokenValid ? NodeConnectionState.REQUESTED : NodeConnectionState.UNAUTHORIZED)
                /*
                 * Set UNKNOWN because we don't now what processing state is,
                 * We know only connection state but we don't know anything about processing state
                 * */
                .nodeProcessingState(NodeProcessingState.UNKNOWN)
                .lastConnectionStateChangeOccurred(dateNow)
                .connectionRequestOccurred(dateNow)
                .nodeIp(nodeConnectionData.getNodeIp())
                .nodeName(nodeConnectionData.getNodeName())
                .nodePort(nodeConnectionData.getNodePort())
                .domainName(nodeConnectionData.getDomainName())
                .transferProtocol(nodeConnectionData.getTransferProtocol())
                .build();
        if (isTokenValid) {
            log.info("Connection Token is correct, trying to add new node connection");
            ClusterConnectionHub.getInstance()
                    .tryAddNodeConnection(nodeConnection);
        } else {
            log.info("Connection Token provided in request is not valid");
        }
        return nodeConnection;
    }

    private boolean validateConnectionToken(String requestedConnectionToken) {
        if (isNull(requestedConnectionToken)) {
            return false;
        }
        return clusterInformation.readConnectToken()
                .map(requestedConnectionToken::equals)
                .orElseGet(() -> false);
    }
}
