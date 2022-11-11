package io.easeci.core.node.connect;

public class NodeConnectionException extends Exception {
    private String message;

    public NodeConnectionException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
