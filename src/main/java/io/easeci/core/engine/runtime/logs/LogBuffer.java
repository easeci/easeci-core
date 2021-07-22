package io.easeci.core.engine.runtime.logs;

import io.easeci.api.socket.Commands;
import io.easeci.core.engine.runtime.PipelineContextLivenessProbe;
import io.easeci.core.workspace.LocationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class LogBuffer implements LogRail, PipelineContextLivenessProbe {
    private static int maxBufferSize;

    private Queue<LogEntry> logEntriesQueue;
    private LogBufferFileManager logBufferFileManager;
    private Consumer<String> logPublisher;
    private boolean isPublishingMode = false;
    private long currentIndex = 0;
    private LocalDateTime logBufferInitTime;
    private UUID pipelineContextId;

    public LogBuffer(UUID pipelineId, UUID pipelineContextId) {
        this.pipelineContextId = pipelineContextId;
        this.logBufferInitTime = LocalDateTime.now();
        this.logEntriesQueue = new LinkedList<>();
        try {
            maxBufferSize = LocationUtils.retrieveFromGeneralInt("output.pipeline-context.buffer-max-size");
            log.info("output.pipeline-context.buffer-max-size = {}", maxBufferSize);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        this.logBufferFileManager = new LogBufferFileManager(this.logEntriesQueue, pipelineId, pipelineContextId);
        this.logBufferFileManager.initScheduler(maxBufferSize);
    }

    private void push(LogEntry logEntry) {
        if (isPublishingMode) {
            logPublisher.accept(logEntry.toString());
        }
    }

    long index() {
        return currentIndex++;
    }

    @Override
    public void initPublishing(Consumer<String> logPublisher) {
        if (logPublisher == null) {
            throw new IllegalStateException("Cannot start publish Logs when logPublisher is not set");
        }
        this.logPublisher = logPublisher;
        this.isPublishingMode = true;
        if (!this.logEntriesQueue.isEmpty()) {
            this.logEntriesQueue.forEach(logEntry -> this.logPublisher.accept(logEntry.toString()));
        }
    }

    @Override
    public void finalizePublishing() {
        this.logBufferFileManager.closeFile();
    }

    @Override
    public void publish(LogEntry logEntry) {
        logEntry.setIndex(index());
        this.push(logEntry);
        this.logEntriesQueue.add(logEntry);
    }

    @Override
    public long entryQueueSize() {
        return this.logEntriesQueue.size();
    }

    @Override
    public String readLog(UUID pipelineContextId, long batchSize, int offset, Commands.LogFetchMode mode) {
        log.info("Reading logs from file for pipelineContextId: {}, batchSize: {}, offset: {}, mode: {}", pipelineContextId, batchSize, offset, mode);
        // TODO
        LogReader logReader = new LogReader();
        return logReader.read(pipelineContextId, batchSize, offset, mode);
    }

    protected Iterator<LogEntry> getQueueIterator() {
        return this.logEntriesQueue.iterator();
    }

    public Optional<Path> getLogFile() {
        return Optional.ofNullable(this.logBufferFileManager.logFilePath);
    }

    @Override
    public boolean isMaximumIdleTimePassed(long clt) {
        return this.logBufferFileManager.isMaximumIdleTimePassed(clt);
    }

    public void closeLogging() {
        this.logBufferFileManager.executorService.shutdown();
    }

    private class LogBufferFileManager {
        private final static int DEFAULT_INTERVAL = 5;
        private final static String FILE_SAVE_INTERVAL_PATH = "output.pipeline-context.file-save-interval";
        private Path logFilePath;
        private ScheduledExecutorService executorService;
        private Queue<LogEntry> logEntryQueue;
        private int logToFileInterval;
        private UUID pipelineId;
        private UUID pipelineContextId;
        // assign time when saved content to file
        private LocalDateTime lastLogFileSaveTry;
        private LocalDateTime lastLogFileSave;

        private LogBufferFileManager(Queue<LogEntry> logEntryQueue, UUID pipelineId, UUID pipelineContextId) {
            try {
                this.logToFileInterval = LocationUtils.retrieveFromGeneralInt(FILE_SAVE_INTERVAL_PATH);
            } catch (Throwable throwable) {
                log.error("Cannot read value of " + FILE_SAVE_INTERVAL_PATH + " so value set to: " + DEFAULT_INTERVAL);
                this.logToFileInterval = DEFAULT_INTERVAL;
            }
            this.logEntryQueue = logEntryQueue;
            this.pipelineId = pipelineId;
            this.pipelineContextId = pipelineContextId;
        }

        void initScheduler(int iterations) {
            this.logFilePath = this.initFile();
            this.executorService = Executors.newSingleThreadScheduledExecutor();
            this.executorService.scheduleAtFixedRate(() -> this.writeToFile(iterations), logToFileInterval, logToFileInterval, TimeUnit.SECONDS);
        }

        private void writeToFile(int iterations) {
            this.lastLogFileSaveTry = LocalDateTime.now();
            if (!this.logEntryQueue.isEmpty()) {
                String linesAggregated = "";
                for (int i = 0; i < iterations; i++) {
                    LogEntry oldestEntry = this.logEntryQueue.poll();
                    if (oldestEntry != null) {
                        linesAggregated = linesAggregated.concat(oldestEntry.toString().concat("\n"));
                    }
                }
                try {
                    Files.write(this.logFilePath, linesAggregated.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    this.lastLogFileSave = LocalDateTime.now();
                } catch (IOException e) {
                    log.error("Cannot write logs to file: " + this.logFilePath.toString());
                    e.printStackTrace();
                }
            } else {
                log.info("There is no logs left in context: " + this.pipelineContextId);
            }
        }

        private Path initFile() {
            final Path logDirPath = LocationUtils.getPipelineRunLogLocation();
            if (Files.notExists(logDirPath)) {
                try {
                    Files.createDirectories(logDirPath);
                } catch (IOException e) {
                    log.info("Could not create directory in path: " + logDirPath);
                    e.printStackTrace();
                }
            }
            final String filePrefix = "/pipeline-run-".concat(this.pipelineId.toString())
                                                     .concat("_")
                                                     .concat(this.pipelineContextId.toString());
            Path readyFilePath = Paths.get(logDirPath.toString().concat(filePrefix));
            try {
                Files.createFile(readyFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return readyFilePath;
        }

        public void closeFile() {
            int iterations = this.logEntryQueue.size();
            writeToFile(iterations);
            log.info("Closed log saving to file for pipelineId: {}, and pipelineContextId: {}", this.pipelineId, this.pipelineContextId);
        }

        public boolean isMaximumIdleTimePassed(long clt) {
            long secondsPassed = this.lastLogFileSave.until(this.lastLogFileSaveTry, ChronoUnit.SECONDS);
            log.info("Idling time for pipeline context id: {} [seconds], is: {}, maximum idle time (CLT): {}", this.pipelineContextId, secondsPassed, clt);
            return secondsPassed >= clt;
        }
    }
}
