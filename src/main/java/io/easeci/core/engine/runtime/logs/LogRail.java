package io.easeci.core.engine.runtime.logs;

import java.util.function.Consumer;

public interface LogRail {

    void initPublishing(Consumer<String> logPublisher);

    /**
     * After invocation of this method logs cannot be published to this on LogRail instance.
     * This is signal that pipeline was finished and any log will not attached.
     * */
    void finalizePublishing();

    void publish(LogEntry logEntry);

    long entryQueueSize();
}
