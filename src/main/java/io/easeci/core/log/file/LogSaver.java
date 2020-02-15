package io.easeci.core.log.file;

import io.easeci.core.output.Event;

import java.nio.file.Path;
import java.util.Queue;
import java.util.function.Predicate;

public abstract class LogSaver {
    private Predicate<Queue<Event>> queuePredicate;
    private Predicate<Event> eventPredicate;
    private Queue<Event> eventQueue;

    public LogSaver(Queue<Event> eventQueue, Predicate<Queue<Event>> queuePredicate, Predicate<Event> eventPredicate) {
        this.eventQueue = eventQueue;
        this.queuePredicate = queuePredicate;
        this.eventPredicate = eventPredicate;
    }

    public abstract Predicate getStrategy();

    public Path save() {
//        Zapisuj jeśli podany w konstruktorze, predefiniowany warunek jest spełniony dla danego eventu
        queuePredicate.test();
        eventPredicate.test();
    }
}
