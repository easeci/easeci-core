package io.easeci.core.node.connect;

import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class ClusterInformationDefault implements ClusterInformation {

    @Override
    public String nodeName() {
        return LocationUtils.retrievePropertyFromGeneral("cluster.master-node.node-name", "");
    }

    @Override
    public String version() {
        return LocationUtils.retrievePropertyFromGeneral("cluster.master-node.application-version", "");
    }

    @Override
    public UUID nodeUuid() {
        try {
            return UUID.fromString(LocationUtils.retrievePropertyFromGeneral("cluster.master-node.node-uuid", ""));
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public String apiVersion() {
        return LocationUtils.retrievePropertyFromGeneral("cluster.master-node.api-version", "");
    }

    @Override
    public String apiVersionPrefix() {
        return LocationUtils.retrievePropertyFromGeneral("cluster.master-node.api-version-prefix", "");
    }

    @Override
    public Optional<String> readConnectToken() {
        Path secretClusterTokenLocation = LocationUtils.getSecretClusterTokenLocation();
        try {
            return Optional.of(Files.readString(secretClusterTokenLocation));
        } catch (IOException e) {
            log.error("Exception occurred while reading connection token", e);
            return Optional.empty();
        }
    }
}
