package io.easeci.core.log.file;

import io.easeci.core.output.Event;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * LogSaver implementation that perform saving logs
 * at concrete moment in time.
 * @author Karol Meksuła
 * 2020-03-04
 * */
public class TimeLogSaver extends LogSaver {
    final static long DEFAULT_TIME_DELAY = 60000;
    private Timer timer;

    public TimeLogSaver(Queue<Event> eventQueue, Path logfile, long logSavingDelay) {
        super(eventQueue, logfile);

        this.timer = new Timer("Log Saving timer", true);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveBatch();
            }
        }, logSavingDelay, logSavingDelay);
    }

    @Override
    public Predicate<Queue<Event>> queuePredicate() {
        return eventQueue -> !eventQueue.isEmpty();
    }

    @Override
    public Path save() {
        return logfile;
    }

    @Override
    public Runnable onShutdown() {
        return this::saveBatch;
    }

    private Path saveBatch() {
        Event event = eventQueue.poll();
        List<byte[]> unmarshaledEvents = new ArrayList<>(Collections.emptyList());
        while (event != null) {
            byte[] unmarshaledEvent = unmarshal(event);
            unmarshaledEvents.add(unmarshaledEvent);
            event = eventQueue.poll();
        }
        return batchWrite(unmarshaledEvents);
    }
}
