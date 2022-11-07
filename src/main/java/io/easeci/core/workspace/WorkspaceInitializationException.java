package io.easeci.core.workspace;

public class WorkspaceInitializationException extends Exception {
    private final String message;

    public WorkspaceInitializationException() {
        this.message = "Workspace initialization exception occurred - cannot start application with invalid files storage";
    }

    public WorkspaceInitializationException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
