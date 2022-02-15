package io.easeci.core.node.connect;

import io.easeci.server.TransferProtocol;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
public class NodeConnection {
    UUID nodeConnectionUuid;
    NodeConnectionState nodeConnectionState;
    Date connectionRequestOccurred;
    Date lastConnectionStateChangeOccurred;
    String nodeIp;
    String nodePort;
    String domainName;
    String nodeName;
    TransferProtocol transferProtocol;

    public NodeConnection recreate(ClusterConnectionStateMonitor.ConnectionStateResponse nodeConnectionState) {
        return NodeConnection.builder()
                .nodeConnectionUuid(this.nodeConnectionUuid)
                .nodeConnectionState(nodeConnectionState.getNodeConnectionState())
                .connectionRequestOccurred(this.connectionRequestOccurred)
                .lastConnectionStateChangeOccurred(new Date())
                .nodeIp(this.nodeIp)
                .nodePort(this.nodePort)
                .domainName(this.domainName)
                .nodeName(this.nodeName)
                .transferProtocol(this.transferProtocol)
                .build();
    }
}
