package io.easeci.core.log;

import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
public class LogTarget {
    private Path logfile;
    private LogSavingStrategy logSavingStrategy;
}
