package io.easeci.core.node.connect;

import io.easeci.core.engine.pipeline.Executor;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ClusterNodesProvider {

    Optional<Executor> findByNodeName(String nodeName);

    Optional<Executor> findByNodeConnectionUuid(UUID nodeConnectionUuid);

    Set<NodeConnection> getReadyToWorkNodes();
}
