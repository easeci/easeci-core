package io.easeci.core.node;

import io.easeci.core.workspace.LocationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public class NodeUtils {

//    TODO in future, when we will implement clustering and master - node architecture and communication
    public static String nodeName() {
        return "easeci-core-0001-master";
    }

    public static String version() {
        return "v0.1-dev";
    }

    public static UUID nodeUuid() {
        return UUID.randomUUID();
    }

    public static Optional<String> readConnectToken() {
        Path secretClusterTokenLocation = LocationUtils.getSecretClusterTokenLocation();
        try {
            return Optional.of(Files.readString(secretClusterTokenLocation));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
