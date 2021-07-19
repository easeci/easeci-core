package io.easeci.core.engine.runtime;

public interface PipelineContextCloser {

    void closeExpiredContexts();
}
