package io.easeci.core.engine.runtime.logs;

import java.util.function.Consumer;

public interface LogRail {

    void onPublish(Consumer<String> logConsumer);

    void initPublishing();

    void publish(LogEntry logEntry);
}
