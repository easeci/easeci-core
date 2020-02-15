package io.easeci.core.log.file;

import io.easeci.core.log.LogSavingStrategy;

import java.nio.file.Path;
import java.util.function.Predicate;

public class OneLogSaver extends LogSaver {

    public OneLogSaver(Predicate<LogSavingStrategy> predicate) {
        super(predicate);
    }

    @Override
    public LogSavingStrategy getStrategy() {
        return null;
    }

    @Override
    public Path save() {
        return null;
    }
}
