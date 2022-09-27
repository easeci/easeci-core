package io.easeci.core.node.connect;

import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.server.TransferProtocol;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
@ToString
public class NodeConnection {
    UUID nodeConnectionUuid;
    NodeConnectionState nodeConnectionState;
    NodeProcessingState nodeProcessingState;
    Date connectionRequestOccurred;
    Date lastConnectionStateChangeOccurred;
    String nodeIp;
    String nodePort;
    String domainName;
    String nodeName;
    TransferProtocol transferProtocol;

    public NodeConnection mapNodeConnection(ConnectionStateResponse nodeConnectionState) {
        return NodeConnection.builder()
                             .nodeConnectionUuid(this.nodeConnectionUuid)
                             .nodeConnectionState(nodeConnectionState.getNodeConnectionState())
                             /*
                              * Set UNKNOWN because we don't now what processing state is,
                              * We know only connection state but we don't know anything about processing state
                              * */
                             .nodeProcessingState(NodeProcessingState.UNKNOWN)
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
