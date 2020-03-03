package io.easeci.core.log;

import io.easeci.core.output.Event;

import java.nio.file.Path;

/**
 * Main interface of log package.
 * This interface is responsible for capturing the event
 * and for processing the event and performing actions in specific implementations.
 * @author Karol Meksuła
 * 2020-02-13
 * */
public interface LogManager {

    /**
     * Handle and process event.
     * @param event is representation of some action in application
     * */
    void handle(Event event);

    /**
     * Initialises file where EaseCI will save all produced log's content
     * */
    Path initLogFile();

    /**
     * Refresh log file, for example when new day just has arrived
     * */
    Path refreshLogFile();

    /**
     * Shutting down gracefully this LogManager instance
     * */
    Path shutdownLogManager();
}
