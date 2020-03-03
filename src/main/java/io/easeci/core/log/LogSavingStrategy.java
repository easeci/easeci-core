package io.easeci.core.log;

/**
 * Enumeration of available log saving strategies in EaseCI application.
 * @author Karol Meksuła
 * 2020-03-03
 * */
public enum LogSavingStrategy {
    EACH,
    BATCH,
    ONE;

    public static LogSavingStrategy getDefault() {
        return LogSavingStrategy.EACH;
    }
}
