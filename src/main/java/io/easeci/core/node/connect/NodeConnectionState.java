package io.easeci.core.node.connect;

import java.util.List;

public enum NodeConnectionState {
    REQUESTED,
    ESTABLISHED,
    CONNECTION_ERROR,
    TIMEOUT,
    /**
     * When node connection state is dead - we could not connect to node after determined attempts
     * */
    DEAD,
    /**
     * Set when cannot authorize node. For instance when connection Token is wrong.
     * */
    UNAUTHORIZED;

    public static List<NodeConnectionState> errorStatuses() {
        return List.of(CONNECTION_ERROR, TIMEOUT, UNAUTHORIZED);
    }
}
