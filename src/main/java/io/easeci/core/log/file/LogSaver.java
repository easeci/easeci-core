package io.easeci.core.log.file;

import io.easeci.core.output.Event;
import io.easeci.utils.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * Main base class that is responsible for implementation of saving logs
 * that was gathered on queue. This abstract class could have many various
 * implementation but has some crucial parts. In order to maintenance unified
 * logs format each child, derived class should use unmarshal(..) method.
 * This class holds also reference to concrete queue of events but is immutable.
 * Class's fields of this abstract class should be visible for all classes in package.
 * Package scope should be kept.
 * @author Karol Meksuła
 * 2020-03-03
 * */
public abstract class LogSaver {
    Predicate<Queue<Event>> queuePredicate;
    Queue<Event> eventQueue;
    Path logfile;

    public LogSaver(Queue<Event> eventQueue, Path logfile) {
        this.eventQueue = eventQueue;
        this.logfile = logfile;
        this.queuePredicate = queuePredicate();
    }

    /**
     * Defines condition when logs should be saved.
     * @return Predicate that tests if event on queue should be saved
     *          just in this moment. Define in this predicate when LogSaver
     *          is obligated to save events to file on local storage.
     * */
    public abstract Predicate<Queue<Event>> queuePredicate();

    /**
     * Pass to LogSaver complete algorithm of saving event to file.
     * @return Path to file in local storage where logs was recently saved.
     * */
    public abstract Path save();

    /**
     * Default saving method that could be use in save() method implementation.
     * Simple saving unmarshalled event to pointed file.
     * @param eventAsBytes is byte representation of Event.class object that should
     *                     be unmarshalled to bytes before.
     * @return Path to file in local storage where logs was recently saved.
     * */
    Path standardWrite(byte[] eventAsBytes) {
        return FileUtils.fileSave(logfile.toString(), new String(eventAsBytes, StandardCharsets.UTF_8), true);
    }

    /**
     * Transform event to byte's array representation,
     * unified for all child classes.
     * @param event is an event that occurred in system taken from queue.
     * @return array of bytes that are representation of Event.class
     * */
    static byte[] unmarshal(Event event) {
        String content = event.getContent();
        Event.EventMeta meta = event.getEventMeta();
        return "[".concat(meta.getPublishTimestamp().toString())
                .concat(", " + meta.getEventType().name() + "] by ")
                .concat(meta.getPublishedBy())
                .concat(", > ")
                .concat(meta.getTitle())
                .concat("\n~")
                .concat(content)
                .concat("\n")
                .getBytes();
    }
}
