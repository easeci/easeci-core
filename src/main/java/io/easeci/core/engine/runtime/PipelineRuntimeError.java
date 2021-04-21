package io.easeci.core.engine.runtime;

public class PipelineRuntimeError extends IllegalStateException {

    private String message;

    public PipelineRuntimeError(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
