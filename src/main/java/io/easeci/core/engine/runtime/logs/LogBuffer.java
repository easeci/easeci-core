package io.easeci.core.engine.runtime.logs;

import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

@Slf4j
public class LogBuffer implements LogRail {
    private static int maxBufferSize;

    private Queue<LogEntry> logEntriesQueue;
    private LogBufferFileManager logBufferFileManager;
    private Consumer<String> logPublisher;
    private boolean isPublishingMode = false;

    public LogBuffer() {
        this.logEntriesQueue = new LinkedList<>();
        try {
            maxBufferSize = LocationUtils.retrieveFromGeneralInt("output.pipeline-context.buffer-max-size");
            log.info("output.pipeline-context.buffer-max-size = {}", maxBufferSize);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        this.logBufferFileManager = new LogBufferFileManager();
        this.logBufferFileManager.init();
    }

    private void push(LogEntry logEntry) {
        if (isPublishingMode) {
            log.info("ws:// object push: " + logEntry.toString());
            logPublisher.accept(logEntry.toString());
        }
    }

    public int index() {
        LogEntry logEntry = logEntriesQueue.peek();
        if (logEntry == null) {
            return 0;
        }
        return logEntry.getIndex() + 1;
    }

    @Override
    public void onPublish(Consumer<String> logPublisher) {
        this.logPublisher = logPublisher;
    }

    @Override
    public void initPublishing() {
        if (logPublisher == null) {
            throw new IllegalStateException("Cannot start publish Logs when logPublisher is not set");
        }
        this.isPublishingMode = true;
        if (!this.logEntriesQueue.isEmpty()) {
            this.logEntriesQueue.forEach(logEntry -> this.logPublisher.accept(logEntry.toString()));
        }
    }

    @Override
    public void publish(LogEntry logEntry) {
        logEntry.setIndex(index());
        this.push(logEntry);
        this.logEntriesQueue.add(logEntry);
        if (logEntriesQueue.size() >= maxBufferSize) {
            LogEntry oldestEntry = logEntriesQueue.poll();
            if (oldestEntry != null) {
                logBufferFileManager.handle(oldestEntry);
            }
        }
    }

    private class LogBufferFileManager {
        private Path logFilePath;

        void handle(LogEntry logEntry) {
            log.info("LOG: " + logEntry.toString());
        }

        public void init() {

        }
    }
}
