package io.easeci.core.log.file;

import io.easeci.core.output.Event;

import java.nio.file.Path;
import java.util.Queue;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

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
        byte[] eventAsBytes = unmarshal(event);
        return standardWrite(eventAsBytes);
    }
}
