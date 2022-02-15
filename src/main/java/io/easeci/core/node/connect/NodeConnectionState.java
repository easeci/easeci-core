package io.easeci.core.node.connect;

public enum NodeConnectionState {
    REQUESTED,
    ESTABLISHED,
    CONNECTION_ERROR,
    TIMEOUT,

    /**
     * Set when cannot authorize node. For instance when connection Token is wrong.
     * */
    UNAUTHORIZED,

    /**
     * IDLE and BUSY are available only if previous state was set as ESTABLISHED
     * */
    IDLE,
    BUSY
}
