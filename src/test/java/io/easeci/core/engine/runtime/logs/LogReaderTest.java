package io.easeci.core.engine.runtime.logs;

import io.easeci.api.socket.Commands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static io.easeci.core.engine.runtime.logs.Utils.extractIndex;
import static io.easeci.core.engine.runtime.logs.Utils.oneStringToLines;
import static org.junit.jupiter.api.Assertions.*;

class LogReaderTest {

    private static final String TEST_LOG_FILE = "logs/pipeline-run-4b74129e-8e1c-429c-8c16-fe86b3cc4571_50679665-ad74-4ac5-9701-1b6e2eeea193";
    private static final UUID PIPELINE_CONTEXT_UUID = UUID.fromString("50679665-ad74-4ac5-9701-1b6e2eeea193");

    @Test
    @DisplayName("Should correctly load file from test/resources and return anything from this file")
    void readLogFileTest() {
        Path testLogFilePath = buildPathFromResources(TEST_LOG_FILE);

        LogReader logReader = new LogReader();
        File file = logReader.findFile(testLogFilePath, PIPELINE_CONTEXT_UUID);

        String line = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            line = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(file);
        assertNotNull(line);
    }

    @Test
    @DisplayName("Should return null value when there is no file with logs of such context with such uuid")
    void readLogFileNotExistsTest() {
        Path testLogFilePath = buildPathFromResources(TEST_LOG_FILE);

        UUID notExistingContext = UUID.randomUUID();

        LogReader logReader = new LogReader();
        File file = logReader.findFile(testLogFilePath, notExistingContext);

        assertNull(file);
    }

    @Test
    @DisplayName("Should correctly read from file first 20 lines")
    void readContentOfLogFileTest() {
        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, 20, 0, Commands.LogFetchMode.HEAD);

        List<String> entries = oneStringToLines(readContent);

        assertAll(() -> assertEquals(20, entries.size()),
                () -> assertEquals(0, extractIndex(entries.get(0))),
                () -> assertEquals(19, extractIndex(entries.get(19))));
    }

    @Test
    @DisplayName("Should correctly read from file last 20 lines")
    void readContentOfFileTailTest() {
        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, 20, 0, Commands.LogFetchMode.TAIL);

        List<String> entries = oneStringToLines(readContent);

        assertAll(() -> assertEquals(20, entries.size()),
                () -> assertEquals(183, extractIndex(entries.get(0))),
                () -> assertEquals(202, extractIndex(entries.get(19))));
    }

    private static Stream<Arguments> values() {
        return Stream.of(
                Arguments.of(0, 0, 19),
                Arguments.of(1, 20, 39),
                Arguments.of(2, 40, 59),
                Arguments.of(3, 60, 79)
        );
    }

    @ParameterizedTest
    @MethodSource("values")
    @DisplayName("Should correctly read from file with offsets")
    void readContentOfLogFileOffsetHeadTest(int offset, int firstIndexOfEntryLog, int lastIndexOfEntryLog) {
        final long batchSize = 20;

        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, batchSize, offset, Commands.LogFetchMode.HEAD);

        List<String> entries = oneStringToLines(readContent);

        assertAll(() -> assertEquals(batchSize, entries.size()),
                () -> assertEquals(firstIndexOfEntryLog, extractIndex(entries.get(0))),
                () -> assertEquals(lastIndexOfEntryLog, extractIndex(entries.get((int) (batchSize - 1)))));
    }

    @Test
    @DisplayName("Should return empty string offset not exists - out of bound")
    void readContentOfLogFileOffsetOutOfBoundTest() {
        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, 20, 20, Commands.LogFetchMode.HEAD);

        assertTrue(readContent.isEmpty());
    }

    @Test
    @DisplayName("Should return correctly only part of file if offset partially out of bound with TAIL mode")
    void readContentOfLogFileOffsetPartiallyOutOfBoundTailTest() {
        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, 20, 10, Commands.LogFetchMode.TAIL);

        List<String> entries = oneStringToLines(readContent);

        // file has 203 lines. We get 20 * 10 = 200 lines. 3 lines should left
        assertEquals(3, entries.size());
        assertAll(() -> assertEquals(0, extractIndex(entries.get(0))),
                () -> assertEquals(1, extractIndex(entries.get(1))),
                () -> assertEquals(2, extractIndex(entries.get(2))));
    }

    @Test
    @DisplayName("Should return correctly only part of file if offset partially out of bound with HEAD mode")
    void readContentOfLogFileOffsetPartiallyOutOfBoundHeadTest() {
        LogReader logReader = new LogReader();
        String readContent = logReader.read(PIPELINE_CONTEXT_UUID, 20, 10, Commands.LogFetchMode.HEAD);

        List<String> entries = oneStringToLines(readContent);

        // file has 203 lines. We get 20 * 10 = 200 lines. 3 lines should left
        assertEquals(3, entries.size());
        assertAll(() -> assertEquals(200, extractIndex(entries.get(0))),
                () -> assertEquals(201, extractIndex(entries.get(1))),
                () -> assertEquals(202, extractIndex(entries.get(2))));
    }
}