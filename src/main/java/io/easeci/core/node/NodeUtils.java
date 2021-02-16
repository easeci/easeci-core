package io.easeci.core.node;

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
}
