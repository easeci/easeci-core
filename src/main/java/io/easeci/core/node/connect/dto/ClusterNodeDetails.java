package io.easeci.core.node.connect.dto;

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
public class ClusterNodeDetails {
    private UUID nodeConnectionUuid;
    private NodeConnectionState nodeConnectionState;
    private Date connectionRequestOccurred;
    private Date lastConnectionStateChangeOccurred;
    private String nodeIp;
    private String nodePort;
    private String domainName;
    private String nodeName;
    private TransferProtocol transferProtocol;
}
