package io.easeci.core.node.connect;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.server.TransferProtocol;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
@ToString
@AllArgsConstructor
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

    @JsonCreator
    public NodeConnection() {
        this.nodeConnectionUuid = null;
        this.nodeConnectionState = null;
        this.nodeProcessingState = null;
        this.connectionRequestOccurred = null;
        this.lastConnectionStateChangeOccurred = null;
        this.nodeIp = null;
        this.nodePort = null;
        this.domainName = null;
        this.nodeName = null;
        this.transferProtocol = null;
    }

    public NodeConnection mapNodeConnection(ConnectionStateResponse nodeConnectionState) {
        return NodeConnection.builder()
                             .nodeConnectionUuid(this.nodeConnectionUuid)
                             .nodeConnectionState(nodeConnectionState.getNodeConnectionState())
                             .nodeProcessingState(nodeConnectionState.getNodeProcessingState())
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
