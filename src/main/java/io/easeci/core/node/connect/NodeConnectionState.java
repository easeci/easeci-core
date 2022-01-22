package io.easeci.core.node.connect;

public enum NodeConnectionState {
    REQUESTED,
    ESTABLISHED,
    CONNECTION_ERROR,
    TIMEOUT,
    UNAUTHORIZED,

    /**
     * IDLE and BUSY are available only if previous state was set as ESTABLISHED
     * */
    IDLE,
    BUSY
}
