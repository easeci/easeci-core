package io.easeci.core.engine.runtime.logs;

import io.easeci.BaseWorkspaceContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static io.easeci.core.engine.runtime.logs.Utils.simplePrintingLogConsumer;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LogBufferTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly add item to LogBuffer")
    void addSuccessTest() {
        LogBuffer logBuffer = new LogBuffer(UUID.randomUUID(), UUID.randomUUID());
        logBuffer.initPublishing(simplePrintingLogConsumer());

        LogEntry logEntry = LogEntry.builder()
                .text("Test log published from LogBufferTest instance")
                .author("system")
                .header("system-log-header")
                .timestamp(Instant.now().getEpochSecond())
                .color(LogEntry.Color.BLACK)
                .endChar(LogEntry.EndChar.NEXT_LINE)
                .fontStyle(LogEntry.FontStyle.NORMAL)
                .fontWeight(LogEntry.FontWeight.NORMAL)
                .logExtent(LogEntry.LogExtent.SYSTEM)
                .build();

        logBuffer.publish(logEntry);
        long entryQueueSize = logBuffer.entryQueueSize();

        assertEquals(1, entryQueueSize);
    }

    @Test
    @DisplayName("Should correctly add few items in correct order to LogBuffer")
    void addSuccessMultipleTest() {
        LogBuffer logBuffer = new LogBuffer(UUID.randomUUID(), UUID.randomUUID());
        logBuffer.initPublishing(simplePrintingLogConsumer());

        LogEntry firstLogEntry = LogEntry.builder()
                .text("First test log published from LogBufferTest instance")
                .author("system")
                .header("system-log-header")
                .timestamp(Instant.now().getEpochSecond())
                .color(LogEntry.Color.BLACK)
                .endChar(LogEntry.EndChar.NEXT_LINE)
                .fontStyle(LogEntry.FontStyle.NORMAL)
                .fontWeight(LogEntry.FontWeight.NORMAL)
                .logExtent(LogEntry.LogExtent.SYSTEM)
                .build();
        logBuffer.publish(firstLogEntry);

        LogEntry secondLogEntry = LogEntry.builder()
                .text("Second test log published from LogBufferTest instance")
                .author("system")
                .header("system-log-header")
                .timestamp(Instant.now().getEpochSecond())
                .color(LogEntry.Color.BLACK)
                .endChar(LogEntry.EndChar.NEXT_LINE)
                .fontStyle(LogEntry.FontStyle.NORMAL)
                .fontWeight(LogEntry.FontWeight.NORMAL)
                .logExtent(LogEntry.LogExtent.SYSTEM)
                .build();
        logBuffer.publish(secondLogEntry);

        LogEntry thirdLogEntry = LogEntry.builder()
                .text("Third test log published from LogBufferTest instance")
                .author("system")
                .header("system-log-header")
                .timestamp(Instant.now().getEpochSecond())
                .color(LogEntry.Color.BLACK)
                .endChar(LogEntry.EndChar.NEXT_LINE)
                .fontStyle(LogEntry.FontStyle.NORMAL)
                .fontWeight(LogEntry.FontWeight.NORMAL)
                .logExtent(LogEntry.LogExtent.SYSTEM)
                .build();
        logBuffer.publish(thirdLogEntry);

        long entryQueueSize = logBuffer.entryQueueSize();
        Iterator<LogEntry> queueIterator = logBuffer.getQueueIterator();

        LogEntry firstItem = queueIterator.next();
        LogEntry secondItem = queueIterator.next();
        LogEntry thirdItem = queueIterator.next();

        assertAll(() -> assertEquals(3, entryQueueSize),
                () -> assertEquals(0, firstItem.getIndex()),
                () -> assertEquals("First test log published from LogBufferTest instance", firstItem.getText()),
                () -> assertEquals(1, secondItem.getIndex()),
                () -> assertEquals("Second test log published from LogBufferTest instance", secondItem.getText()),
                () -> assertEquals(2, thirdItem.getIndex()),
                () -> assertEquals("Third test log published from LogBufferTest instance", thirdItem.getText())
        );
    }

    @Test
    @DisplayName("Should correctly publishing cycle works and log should be publishing in correct order")
    void publishingCycleTest() {
        LogBuffer logBuffer = new LogBuffer(UUID.randomUUID(), UUID.randomUUID());
        logBuffer.initPublishing(simplePrintingLogConsumer());

        final int ITERATIONS = 100;
//        Random random = new Random();
//        long sleepSeconds = random.nextInt(10);

        for (int i = 0; i < ITERATIONS; i++) {
            LogEntry logEntry = LogEntry.builder()
                    .text("Test log published from LogBufferTest instance, no. " + i)
                    .author("system")
                    .header("system-log-header")
                    .timestamp(Instant.now().getEpochSecond())
                    .color(LogEntry.Color.BLACK)
                    .endChar(LogEntry.EndChar.NEXT_LINE)
                    .fontStyle(LogEntry.FontStyle.NORMAL)
                    .fontWeight(LogEntry.FontWeight.NORMAL)
                    .logExtent(LogEntry.LogExtent.SYSTEM)
                    .build();
            logBuffer.publish(logEntry);
//            uncomment if you want to simulate time intervals between logs adding
//            try {
//                Thread.sleep(sleepSeconds * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        logBuffer.finalizePublishing();
        Path tmpLogFile = Path.of("/tmp/easeci-logs/log");
        try {
            Files.createDirectories(Path.of("/tmp/easeci-logs"));
            tmpLogFile = Files.notExists(tmpLogFile) ? Files.createFile(tmpLogFile) : tmpLogFile;
            Files.copy(logBuffer.getLogFile().orElseThrow(), tmpLogFile, StandardCopyOption.REPLACE_EXISTING);
            List<String> lines = Files.readAllLines(tmpLogFile);
            assertEquals(ITERATIONS, lines.size());
//            comment line bellow to watch the file after test in debug order
//            Files.deleteIfExists(tmpLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}