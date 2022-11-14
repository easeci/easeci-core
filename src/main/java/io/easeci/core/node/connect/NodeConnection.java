package io.easeci.core.node.connect;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.easeci.core.engine.pipeline.Executor;
import io.easeci.core.node.connect.dto.ConnectionStateResponse;
import io.easeci.server.TransferProtocol;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
@ToString
@AllArgsConstructor
public class NodeConnection implements Executor {
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
    int connectionAttemptsCounter;

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
        this.connectionAttemptsCounter = 0;
    }

    public NodeConnection mapNodeConnection(ConnectionStateResponse nodeConnectionState, int connectionAttemptsCounter) {
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
                             .connectionAttemptsCounter(connectionAttemptsCounter)
                             .build();
    }

    @Override
    public UUID getNodeUuid() {
        return nodeConnectionUuid;
    }
}
