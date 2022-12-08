package io.easeci.core.node.connect;

import io.easeci.server.CommunicationType;
import io.easeci.server.TransferProtocol;

import java.util.Optional;
import java.util.UUID;

public interface ClusterInformation {

    String domainName();

    String nodeName();

    String version();

    UUID nodeUuid();

    String apiVersion();

    String apiVersionPrefix();

    Optional<String> readConnectToken();

    TransferProtocol transferProtocol();

    /**
     * This method should return value of CommunicationType,
     * then worker nodes will communicate to master node via ip, ip with port or domain name.
     * */
    CommunicationType communicationType();

    String ip();

    String port();
}
