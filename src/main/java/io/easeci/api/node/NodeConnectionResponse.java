package io.easeci.api.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.api.Errorable;
import io.easeci.core.node.connect.NodeConnectionState;
import io.easeci.server.TransferProtocol;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeConnectionResponse extends Errorable {
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
