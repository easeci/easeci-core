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

import static java.util.Objects.isNull;

@Slf4j
public class LogBuffer implements LogRail, PipelineContextLivenessProbe {
    private static int maxBufferSize;

    private Queue<LogEntry> logEntriesQueue;
    private LogBufferFileManager logBufferFileManager;
    private Consumer<String> logPublisher;
    private boolean isPublishingMode = false;
    private long currentIndex = 0;
    private UUID pipelineId;
    private UUID pipelineContextId;

    // use it for read-only from file mode
    public LogBuffer() {}

    public LogBuffer(UUID pipelineId, UUID pipelineContextId) {
        this.pipelineId = pipelineId;
        this.pipelineContextId = pipelineContextId;
        this.logEntriesQueue = new LinkedList<>();
        try {
            maxBufferSize = LocationUtils.retrieveFromGeneralInt("output.pipeline-context.buffer-max-size");
            log.info("output.pipeline-context.buffer-max-size = {}", maxBufferSize);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void push(LogEntry logEntry) {
        if (isPublishingMode) {
            logPublisher.accept(logEntry.toString());
        }
    }

    long index() {
        return currentIndex++;
    }

    public LogBuffer initLogBufferManager() {
        log.info("Started to handling logs, buffering it and publishing");
        this.logBufferFileManager = new LogBufferFileManager(this.logEntriesQueue, pipelineId, pipelineContextId);
        this.logBufferFileManager.initScheduler(maxBufferSize);
        return this;
    }

    @Override
    public void initPublishing(Consumer<String> logPublisher, Options... options) {
        if (logPublisher == null) {
            throw new IllegalStateException("Cannot start publish Logs when logPublisher is not set");
        }
        if (isNull(logBufferFileManager)) {
            throw new IllegalStateException("Cannot start publish Logs when LogBufferFileManager is not initialized. " +
                    "Invoke initLogBufferManager() first of all to initialize");
        }

        List<Options> optionsList = Arrays.asList(options);
        this.logPublisher = logPublisher;
        this.isPublishingMode = true;
        if (optionsList.contains(Options.NO_OFFSET)) {
            this.readMissingLogsFromFile();
            this.readRemainingLogsInCache();
        }
        //default option
        if (optionsList.isEmpty() || optionsList.contains(Options.OFFSET)) {
            this.readRemainingLogsInCache();
        }
    }

    // This method should read and publish logs that are gathered only in this instance object's list 'logEntriesQueue'
    private void readRemainingLogsInCache() {
        if (!this.logEntriesQueue.isEmpty()) {
            this.logEntriesQueue.forEach(logEntry -> this.logPublisher.accept(logEntry.toString()));
        }
    }

    private void readMissingLogsFromFile() {
//        long index = assignLastLogIndexPublished();
        LogReader logReader = new LogReader();
        logReader.readTail(this.logBufferFileManager.pipelineContextId, 1000)
                 .forEach(line -> logPublisher.accept(line));
    }

    private long assignLastLogIndexPublished() {
        return Optional.ofNullable(this.logEntriesQueue.peek())
                       .orElse(LogEntry.builder()
                               .index(0)
                               .build())
                       .getIndex();
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
        if (isNull(this.logBufferFileManager)) {
            log.info("logBufferFileManager because is null");
            return false;
        }
        return this.logBufferFileManager.isMaximumIdleTimePassed(clt);
    }

    public void closeLogging() {
        if (isNull(this.logBufferFileManager)) {
            log.info("Cannot close logBufferFileManager because is null");
        } else {
            this.logBufferFileManager.executorService.shutdown();
            this.logBufferFileManager.closeFile();
        }
    }

    public enum Options {
        NO_OFFSET,
        OFFSET
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
                    log.error("Error: ", e);
                }
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
                // todo tutaj występuje jakaś kolizja, dostaję
//                java.nio.file.FileAlreadyExistsException: /home/karol/easeci-backup/log/context/pipeline-run-cc4fec7a-1b5f-4bc2-8213-9b08ae794514_f22dcf81-a686-4f37-a4c7-0c16219309b5
                if (Files.exists(readyFilePath)) {
                    log.info("File already exists: {}", readyFilePath);
                } else {
                    Files.createFile(readyFilePath);
                }
            } catch (IOException e) {
                log.error("IOException occurred: ", e);
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
