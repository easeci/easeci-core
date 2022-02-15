package io.easeci.core.node.connect;

import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@AllArgsConstructor
public class ClusterConnectionStateMonitor {
    private NodeConnector nodeConnector;

    public ConnectionStateResponse checkWorkerState(ConnectionStateRequest connectionStateRequest) {
        return nodeConnector.initialCallback(connectionStateRequest);
    }

    @Value(staticConstructor = "of")
    public static class ConnectionStateRequest {
        String nodeIp;
        String nodePort;
        String domainName;
        String nodeName;
        TransferProtocol transferProtocol;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public class ConnectionStateResponse extends ConnectionStateRequest {
        NodeConnectionState nodeConnectionState;

        private ConnectionStateResponse(String nodeIp, String nodePort, String domainName, String nodeName,
                                        TransferProtocol transferProtocol, NodeConnectionState nodeConnectionState) {
            super(nodeIp, nodePort, domainName, nodeName, transferProtocol);
            this.nodeConnectionState = nodeConnectionState;
        }
    }
}
