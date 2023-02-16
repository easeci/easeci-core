package io.easeci.core.node.connect.dto;

import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.core.node.connect.NodeProcessingState;
import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ConnectionStateResponse {
    private NodeConnectionState nodeConnectionState;
    private NodeProcessingState nodeProcessingState;
    private String nodeIp;
    private String nodePort;
    private String domainName;
    private String nodeName;
    private UUID nodeId;
    private TransferProtocol transferProtocol;

    public static ConnectionStateResponse error(ConnectionStateRequest connectionStateRequest, NodeConnectionState nodeConnectionState) {
        return ConnectionStateResponse.of(
                nodeConnectionState,
                NodeProcessingState.UNKNOWN,
                connectionStateRequest.getNodeIp(),
                connectionStateRequest.getNodePort(),
                connectionStateRequest.getDomainName(),
                connectionStateRequest.getNodeName(),
                null,
                connectionStateRequest.getTransferProtocol()
        );
    }
}