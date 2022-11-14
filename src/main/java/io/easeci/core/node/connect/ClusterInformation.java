package io.easeci.core.node.connect;

import java.util.Optional;
import java.util.UUID;

public interface ClusterInformation {

    String nodeName();

    String version();

    UUID nodeUuid();

    String apiVersion();

    String apiVersionPrefix();

    Optional<String> readConnectToken();
}
