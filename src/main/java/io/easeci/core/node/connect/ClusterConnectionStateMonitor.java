package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateRequest;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import lombok.*;

@AllArgsConstructor
public class ClusterConnectionStateMonitor {
    private NodeConnector nodeConnector;

    public ConnectionStateResponse checkWorkerState(ConnectionStateRequest connectionStateRequest) {
        return nodeConnector.initialCallback(connectionStateRequest);
    }

    /**
     * Use this method to create ConnectionStateResponse when there was fail to connect worker node
     * */
    public static ConnectionStateResponse createResponseFailure(NodeConnectionState connectionState, ConnectionStateRequest connectionStateRequest) {
        return ConnectionStateResponse.of(connectionState, connectionStateRequest.getNodeIp(), connectionStateRequest.getNodePort(), connectionStateRequest.getDomainName(),
                                           connectionStateRequest.getNodeName(), connectionStateRequest.getTransferProtocol());
    }
}
