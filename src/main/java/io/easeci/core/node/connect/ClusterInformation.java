package io.easeci.core.node.connect;

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
}
