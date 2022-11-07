package io.easeci.core.node.connect.dto;

import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.core.node.connect.NodeProcessingState;
import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private TransferProtocol transferProtocol;
}