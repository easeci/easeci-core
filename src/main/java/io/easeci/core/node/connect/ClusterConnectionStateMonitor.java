package io.easeci.core.node.connect;

import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
public class ClusterConnectionStateMonitor {
    private NodeConnector nodeConnector;

    public NodeConnectionState checkWorkerState(ConnectionStateRequest connectionStateRequest) {
        nodeConnector.initialCallback(connectionStateRequest);
        return null; //todo
    }

    @Value(staticConstructor = "of")
    public static class ConnectionStateRequest {
        String nodeIp;
        String nodePort;
        String domainName;
        String nodeName;
        TransferProtocol transferProtocol;
    }
}
