package io.easeci.core.engine.runtime.logs;

import io.easeci.api.socket.Commands;

import java.util.UUID;
import java.util.function.Consumer;

public interface LogRail {

    /**
     * @param logPublisher defines a way how logs will be consumed
     * @param options defines and additional and optional options for log publishing
     * */
    void initPublishing(Consumer<String> logPublisher, LogBuffer.Options... options);

    /**
     * After invocation of this method logs cannot be published to this on LogRail instance.
     * This is signal that pipeline was finished and any log will not attached.
     * */
    void finalizePublishing();

    void publish(LogEntry logEntry);

    long entryQueueSize();

    String readLog(UUID pipelineContextId, long batchSize, int offset, Commands.LogFetchMode mode);
}
