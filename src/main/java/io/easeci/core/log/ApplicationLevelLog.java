package io.easeci.core.log;

import io.easeci.core.configuration.ConfigurationRealm;
import io.easeci.core.configuration.MainConfigurationRealm;
import io.easeci.core.log.file.LogSaver;
import io.easeci.core.output.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ApplicationLevelLog implements LogManager {
    private static ApplicationLevelLog applicationLevelLog;
    private Queue<Event> eventQueue;
    private LogTarget logTarget;
    private LogSaver logSaver;

    ApplicationLevelLog(ConfigurationRealm configurationRealm) {
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.logTarget = (LogTarget) configurationRealm.retrieve(LogTarget.class);
    }

    public static ApplicationLevelLog getInstance() {
        if (isNull(applicationLevelLog)) {
            ApplicationLevelLog.applicationLevelLog = new ApplicationLevelLog(MainConfigurationRealm.getInstance());
        }
        return applicationLevelLog;
    }

    @Override
    public void handle(Event event) {
        eventQueue.add(event);
        logSaver.save();
    }

    private String filename() {
        return null;
    }

}
