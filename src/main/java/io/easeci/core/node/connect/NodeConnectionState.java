package io.easeci.core.node.connect;

import java.util.List;

public enum NodeConnectionState {
    REQUESTED,
    ESTABLISHED,
    CONNECTION_ERROR,
    TIMEOUT,
    /**
     * When node was not even requested for status
     * */
    NOT_CHANGED,
    /**
     * When node connection state is dead - we could not connect to node after determined attempts
     * */
    DEAD,
    /**
     * Set when cannot authorize node. For instance when connection Token is wrong
     * */
    UNAUTHORIZED,

    /**
     * We don't know nothing yet about connection state with worker node
     * */
    UNKNOWN;

    public static List<NodeConnectionState> errorStatuses() {
        return List.of(CONNECTION_ERROR, TIMEOUT, UNAUTHORIZED);
    }
}
