package io.easeci.core.log.file;

import io.easeci.core.output.Event;
import io.easeci.utils.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.easeci.core.log.file.EventUtils.provideEvent;
import static io.easeci.core.log.file.LogSaver.unmarshal;
import static org.junit.jupiter.api.Assertions.*;

class LogSaverTest {
    private LogSaver logSaver;
    private Queue<Event> eventQueue;
    private String FILEPATH = "/tmp/easeci_log_test";
    private Path logfile = Paths.get("/tmp/easeci_log_test");

    @BeforeEach
    void setup() {
        this.eventQueue = new LinkedList<>();
        this.logfile = Paths.get(FILEPATH);

        /**
         * This object is created only for testing base-class's methods.
         * Concrete implementation is not important in this case.
         * */
        logSaver = new LogSaver(eventQueue, logfile) {
            @Override
            public Predicate<Queue<Event>> queuePredicate() {
                return null;
            }

            @Override
            public Path save() {
                return null;
            }

            @Override
            public Runnable onShutdown() {
                return null;
            }
        };

        FileUtils.fileSave(FILEPATH, "", true);
    }

    static Set<Event> provideSameEvents() {
        return Stream.generate(EventUtils::provideEvent)
                .limit(3)
                .collect(Collectors.toSet());
    }

    @ParameterizedTest
    @MethodSource(value = "provideSameEvents")
    @DisplayName("Should return in each case same value of bytes array length, arrays must be the same")
    void unmarshalTest(Event event) {
        final int ARRAY_SIZE = 117;

        byte[] unmarshal = unmarshal(event);

        assertEquals(ARRAY_SIZE, unmarshal.length);
    }

    @Test
    @DisplayName("Should return empty bytes array when event passed as a method's argument is null")
    void unmarshalNullTest() {
        final int EXPECTED_ARRAY_SIZE = 0;
        Event event = null;

        byte[] unmarshal = unmarshal(event);

        assertEquals(EXPECTED_ARRAY_SIZE, unmarshal.length);
    }

    @Test
    @DisplayName("Should correctly write unmarshalled event to file, length of saved bytes should be the same as unmarshalled event")
    void standardWriteTest() {
        final int EXPECTED_BYTES_SIZE = 117;
        Event event = provideEvent();

        byte[] unmarshal = unmarshal(event);
        Path path = logSaver.standardWrite(unmarshal);
        byte[] fileContent = FileUtils.fileLoad(path.toString()).getBytes();

        assertAll(() -> assertEquals(EXPECTED_BYTES_SIZE, unmarshal.length),
                () -> assertTrue(FileUtils.isExist(path.toString())),
                () -> assertEquals(unmarshal.length, fileContent.length));
    }

    @Test
    @DisplayName("Should return the same path when pass null in method argument as correct argument")
    void standardWriteNullTest() {
        byte[] unmarshal = null;

        Path path = logSaver.standardWrite(unmarshal);

        assertAll(() -> assertEquals(path, logfile),
                () -> assertDoesNotThrow(() -> NullPointerException.class));
    }

    @Test
    @DisplayName("Should write few events as a batch")
    void batchWriteTest() {
        final int EVENT_AMOUNT = 4;
        final int EVENT_SIZE = 117;
        final int EXPECTED_TOTAL_BYTES = EVENT_AMOUNT * EVENT_SIZE;
        List<byte[]> unmarshalEvents = Stream.generate(EventUtils::provideEvent)
                .limit(EVENT_AMOUNT)
                .collect(Collectors.toList())
                .stream()
                .map(LogSaver::unmarshal)
                .collect(Collectors.toList());

        Path path = logSaver.batchWrite(unmarshalEvents);

        byte[] readBytesFromFile = FileUtils.fileLoad(path.toString()).getBytes();

        assertEquals(EXPECTED_TOTAL_BYTES, readBytesFromFile.length);
    }

    @Test
    @DisplayName("Should")
    void batchWriteNullTest() {
        final int EXPECTED_BYTE_SIZE = 0;
        List<byte[]> bytesEmpty = Collections.emptyList();
        List<byte[]> bytesNull = null;

        Path path = logSaver.batchWrite(bytesEmpty);
        Path path1 = logSaver.batchWrite(bytesNull);

        assertAll(() -> assertEquals(path, path1),
                () -> assertDoesNotThrow(() -> NullPointerException.class),
                () -> assertEquals(EXPECTED_BYTE_SIZE, FileUtils.fileLoad(path.toString()).getBytes().length));
    }

    @AfterEach
    void cleanup() {
        FileUtils.fileDelete(FILEPATH);
    }
}