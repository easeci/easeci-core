package io.easeci.core.node.connect;

import io.easeci.server.TransferProtocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConnectionData {
    private String nodeIp;
    private String nodePort;
    private String domainName;
    private String nodeName;
    private TransferProtocol transferProtocol;
    private String connectionToken;
}
