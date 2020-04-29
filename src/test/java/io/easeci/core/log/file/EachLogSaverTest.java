package io.easeci.core.log.file;

import io.easeci.core.output.Event;
import io.easeci.commons.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class EachLogSaverTest {
    private final static String FILEPATH = "/tmp/test-logfile";

    @Test
    @DisplayName("Should initialize EachLogSaver correctly")
    void eachLogSaverInitTest() {
        Queue<Event> eventQueue = new LinkedList<>();

        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));

        assertAll(() -> assertEquals(FILEPATH, logSaver.logfile.toString()),
                () -> assertEquals(0, logSaver.eventQueue.size()),
                () -> assertNotNull(logSaver.queuePredicate));
    }

    @Test
    @DisplayName("Should predicate return true when queue has items")
    void predicateQueueHasItems() {
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));
        eventQueue.add(EventUtils.provideEvent());

        boolean test = logSaver.queuePredicate.test(eventQueue);

        assertTrue(test);
    }

    @Test
    @DisplayName("Should predicate return false when queue has any items")
    void predicateQueueHasAnyItems() {
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));

        boolean test = logSaver.queuePredicate.test(eventQueue);

        assertFalse(test);
    }

    @Test
    @DisplayName("Should save event in every each occurrence correctly")
    void eachLogSaverSavingTest() {
        final int TIMES = 4;
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));

        for (int i = 0; i < TIMES; i++) {
            Event event = EventUtils.provideEvent();
            eventQueue.add(event);

            logSaver.save();
        }

        assertAll(() -> assertTrue(FileUtils.isExist(FILEPATH)),
                () -> assertEquals(TIMES * EventUtils.EVENT_BYTE_SIZE, FileUtils.fileLoad(FILEPATH).getBytes().length));
    }

    @Test
    @DisplayName("Should not saving event if it is null value")
    void eachLogSaverSavingNullValueTest() {
        final int TIMES = 4,
                EXPECTED_LOGS_BYTE_SIZE = (TIMES - 1) * EventUtils.EVENT_BYTE_SIZE;

        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));

        for (int i = 0; i < TIMES; i++) {
            Event event = EventUtils.provideEvent();
            if (i == 3) {
                event = null;
            }
            eventQueue.add(event);

            logSaver.save();
        }

        assertAll(() -> assertTrue(FileUtils.isExist(FILEPATH)),
                () -> assertEquals(EXPECTED_LOGS_BYTE_SIZE, FileUtils.fileLoad(FILEPATH).getBytes().length));
    }

    @Test
    @DisplayName("Should save all remaining events on queue while shutting down application")
    void eachLogSaverShutdownTest() {
        final int TIMES = 4;
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new EachLogSaver(eventQueue, Paths.get(FILEPATH));

        for (int i = 0; i < TIMES; i++) {
            Event event = EventUtils.provideEvent();
            eventQueue.add(event);
        }

//        here logSaver should save all remaining events
        logSaver.onShutdown().run();

        assertAll(() -> assertTrue(FileUtils.isExist(FILEPATH)),
                () -> assertEquals(TIMES * EventUtils.EVENT_BYTE_SIZE, FileUtils.fileLoad(FILEPATH).getBytes().length));
    }

    @AfterEach
    void cleanup() {
        FileUtils.fileDelete(FILEPATH);
    }
}