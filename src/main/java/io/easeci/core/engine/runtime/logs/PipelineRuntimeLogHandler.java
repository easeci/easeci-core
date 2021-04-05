package io.easeci.core.engine.runtime.logs;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface PipelineRuntimeLogHandler {

    void handle() throws InterruptedException, TimeoutException, IOException;
}
