package io.easeci.api.node;

import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConnectionResponse {
    UUID nodeConnectionUuid;
    NodeConnectionState nodeConnectionState;
    Date connectionRequestOccurred;
    Date lastConnectionStateChangeOccurred;
    String nodeIp;
    String nodePort;
    String domainName;
    String nodeName;
    TransferProtocol transferProtocol;
}
