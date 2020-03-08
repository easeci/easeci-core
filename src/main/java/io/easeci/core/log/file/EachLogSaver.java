package io.easeci.core.log.file;

import io.easeci.core.output.Event;

import java.nio.file.Path;
import java.util.Queue;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

/**
 * LogSaver implementation that perform saving logs per
 * each event occurrence.
 * @author Karol Meksuła
 * 2020-03-03
 * */
public class EachLogSaver extends LogSaver {

    public EachLogSaver(Queue<Event> eventQueue, Path logfile) {
        super(eventQueue, logfile);
    }

    @Override
    public Predicate<Queue<Event>> queuePredicate() {
        return eventQueue -> {
            if (eventQueue.isEmpty())
                return false;
            if (isNull(eventQueue.peek()))
                return false;
            return true;
        };
    }

    @Override
    public Path save() {
        Event event = eventQueue.poll();
        if (isNull(event)) {
            return logfile;
        }
        byte[] eventAsBytes = unmarshal(event);
        return standardWrite(eventAsBytes);
    }

    @Override
    public Runnable onShutdown() {
        return () -> {
            while (queuePredicate.test(eventQueue)) {
                this.save();
            }
        };
    }
}
