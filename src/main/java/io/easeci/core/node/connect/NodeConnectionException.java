package io.easeci.core.node.connect;

public class NodeConnectionException extends Exception {
    private String message;

    public NodeConnectionException(String message) {
        super(message);
    }
}
