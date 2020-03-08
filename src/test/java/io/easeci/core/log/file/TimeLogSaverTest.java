package io.easeci.core.log.file;

import io.easeci.core.output.Event;
import io.easeci.utils.io.FileUtils;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class TimeLogSaverTest {
    private final static String FILEPATH = "/tmp/test-logfile";

    @Test
    @DisplayName("Should initialize TimeLogSaver correctly")
    void timeLogSaverInitTest() {
        final long TIME_DELAY = 1000;
        Queue<Event> eventQueue = new LinkedList<>();

        LogSaver logSaver = new TimeLogSaver(eventQueue, Paths.get(FILEPATH), TIME_DELAY);

        assertAll(() -> assertEquals(FILEPATH, logSaver.logfile.toString()),
                () -> assertEquals(0, logSaver.eventQueue.size()),
                () -> assertNotNull(logSaver.queuePredicate));
    }

    @Test
    @DisplayName("Should predicate return true when queue has items")
    void predicateQueueHasItems() {
        final long TIME_DELAY = 1000;
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new TimeLogSaver(eventQueue, Paths.get(FILEPATH), TIME_DELAY);
        eventQueue.add(EventUtils.provideEvent());

        boolean test = logSaver.queuePredicate.test(eventQueue);

        assertTrue(test);
    }

    @Test
    @DisplayName("Should predicate return false when queue has any items")
    void predicateQueueHasAnyItems() {
        final long TIME_DELAY = 1000;
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new TimeLogSaver(eventQueue, Paths.get(FILEPATH), TIME_DELAY);

        boolean test = logSaver.queuePredicate.test(eventQueue);

        assertFalse(test);
    }

    @Test
    @DisplayName("Should save() method do nothing but only return logfile path")
    void timeLogSaverDummySaveMethodTest() {
        final long TIME_DELAY = 1000;
        Queue<Event> eventQueue = new LinkedList<>();
        LogSaver logSaver = new TimeLogSaver(eventQueue, Paths.get(FILEPATH), TIME_DELAY);

        Path path = logSaver.save();

        assertAll(() -> assertEquals(path.toString(), FILEPATH),
                () -> assertFalse(FileUtils.isExist(FILEPATH)));
    }

    @Test
    @DisplayName("Should invoke saving method after TIME_DELAY is up and correctly save events from queue")
    void timeLogSaverBatchSaveTest() throws InterruptedException {
        final long TIME_DELAY = 1000;
        final int TIMES = 4;
        Queue<Event> eventQueue = new LinkedList<>();

        for (int i = 0; i < TIMES; i++) {
            Event event = EventUtils.provideEvent();
            eventQueue.add(event);
        }

        LogSaver logSaver = new TimeLogSaver(eventQueue, Paths.get(FILEPATH), TIME_DELAY);

//        logSaver should invoke automatically after TIME_DELAY up
        Thread.sleep(TIME_DELAY + 500);

//        Why 'or' operator?
//        Because TimeLogSaver has a handler that saving remaining events on queue before object destroy
//        So there are two possible results
        assertAll(() -> assertTrue(FileUtils.isExist(FILEPATH)),
                () -> assertTrue(TIMES * EventUtils.EVENT_BYTE_SIZE == FileUtils.fileLoad(FILEPATH).getBytes().length
                        || 585 == FileUtils.fileLoad(FILEPATH).getBytes().length));
    }

    @AfterEach
    void cleanup() {
        FileUtils.fileDelete(FILEPATH);
    }
}
