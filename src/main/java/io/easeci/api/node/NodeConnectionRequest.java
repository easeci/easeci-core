package io.easeci.api.node;

import io.easeci.server.TransferProtocol;
import lombok.Data;

@Data
public class NodeConnectionRequest {
    private String nodeIp;
    private String nodeName;
    private String nodePort;
    private String domainName;
    private TransferProtocol transferProtocol;
    private String connectionToken;
}
