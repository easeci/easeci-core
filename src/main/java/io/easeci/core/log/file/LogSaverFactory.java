package io.easeci.core.log.file;

import io.easeci.core.log.LogSavingStrategy;
import io.easeci.core.output.Event;

import java.nio.file.Path;
import java.util.Queue;

import static io.easeci.core.log.file.TimeLogSaver.DEFAULT_TIME_DELAY;
import static io.easeci.core.workspace.WorkspaceUtils.retrieveFromGeneral;
import static io.easeci.core.workspace.WorkspaceUtils.retrieveFromGeneralInt;
import static java.util.Objects.isNull;

/**
 * Factory class that is responsible for produce concrete LogSaver
 * based on delivered LogSavingStrategy as a first method's argument
 * @author Karol Meksuła
 * 2020-03-03
 * */
public class LogSaverFactory {

    public static LogSaver factorize(LogSavingStrategy strategy, Queue<Event> eventQueue, Path logfile) {
        valid(strategy, eventQueue);
        if (strategy.equals(LogSavingStrategy.TIME)) {
            long batchTimeDelay;
            try {
                batchTimeDelay = retrieveFromGeneralInt("log.delay");
            } catch (Throwable throwable) {
                batchTimeDelay = DEFAULT_TIME_DELAY;
                throwable.printStackTrace();
            }
            return new TimeLogSaver(eventQueue, logfile, batchTimeDelay);
        }
        if (strategy.equals(LogSavingStrategy.BATCH)) {

        }
        if (strategy.equals(LogSavingStrategy.EACH)) {
            return new EachLogSaver(eventQueue, logfile);
        }
        throw new RuntimeException("No matching enum class has found.");
    }

    private static void valid(LogSavingStrategy strategy, Queue<Event> eventQueue) {
        if (isNull(strategy)) {
            throw new RuntimeException("Cannot factorize LogSaver.class instance because LogSavingStrategy is null!");
        }
        if (isNull(eventQueue)) {
            throw new RuntimeException("Cannot factorize LogSaver.class instance because Queue<Event> is null!");
        }
    }
}
