package io.easeci.core.log.file;

import io.easeci.core.output.Event;

import java.util.Queue;
import java.util.function.Predicate;

public class OneLogSaver extends LogSaver {

    public OneLogSaver(Queue<Event> eventQueue, Predicate<Queue<Event>> queuePredicate, Predicate<Event> eventPredicate) {
        super(eventQueue, queuePredicate, eventPredicate);
    }

    @Override
    public Predicate getStrategy() {
        return null;
    }
}
