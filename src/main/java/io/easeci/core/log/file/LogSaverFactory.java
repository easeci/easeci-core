package io.easeci.core.log.file;

import io.easeci.core.log.LogSavingStrategy;

import static java.util.Objects.isNull;

public class LogSaverFactory {

    public LogSaver factorize(LogSavingStrategy strategy) {
        if (isNull(strategy)) {
            throw new RuntimeException("Cannot factorize LogSaver.class instance because argument method is null!");
        }
        if (strategy.equals(LogSavingStrategy.ONE)) {

        }
        if (strategy.equals(LogSavingStrategy.BATCH)) {

        }
        if (strategy.equals(LogSavingStrategy.EACH)) {

        }
        throw new RuntimeException("No matching enum class has found.");
    }
}
