package com.meksula;

import io.easeci.extension.ExtensionType;
import io.easeci.extension.Standalone;
import io.easeci.extension.State;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimePrinter implements Standalone {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public void start() {
        executorService.scheduleAtFixedRate(() -> System.out.printf("\n===> [time-printer] %s", LocalDateTime.now()), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {

    }

    @Override
    public State state() {
        return null;
    }

    @Override
    public String about() {
        return null;
    }

    @Override
    public ExtensionType type() {
        return ExtensionType.STANDALONE_PLUGIN;
    }
}
