package io.easeci.core.log;

import io.easeci.core.log.file.LogSaver;
import io.easeci.core.log.file.LogSaverFactory;
import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import io.easeci.core.workspace.LocationUtils;
import io.easeci.commons.DirUtils;
import io.easeci.commons.FileUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.easeci.core.log.Publishers.SYSTEM;
import static java.util.Objects.isNull;

@Slf4j
public class ApplicationLevelLog implements LogManager {
    public static final String LOGFILE_PREFIX = "easeci-logs-",
                                LOG_DIRECTORY = "/log/";
    private static ApplicationLevelLog applicationLevelLog;
    private Queue<Event> eventQueue;
    private LogSaver logSaver;
    private Timer logDaemon;

    @Getter
    private Path currentLogfile;

    private ApplicationLevelLog() {
        this.currentLogfile = initLogFile();
        this.eventQueue = new ConcurrentLinkedQueue<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.handle(Event.builder()
                    .eventMeta(Event.EventMeta.builder()
                            .eventType(EventType.RUNTIME)
                            .title("Bye bye")
                            .publishTimestamp(LocalDateTime.now())
                            .publishedBy(SYSTEM.name())
                            .build())
                    .content("EaseCI system is shutting down gracefully, bye!")
                    .build());
            shutdownLogManager();
        }));
    }

    public static ApplicationLevelLog getInstance() {
        if (isNull(applicationLevelLog)) {
            ApplicationLevelLog.applicationLevelLog = new ApplicationLevelLog();
            LogSavingStrategy savingStrategy;
            try {
                savingStrategy = LogSavingStrategy.valueOf(LocationUtils.retrieveFromGeneral("log.logSavingStrategy")
                        .trim()
                        .toUpperCase());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                savingStrategy = LogSavingStrategy.getDefault();
            }
            ApplicationLevelLog.applicationLevelLog.logSaver = LogSaverFactory.factorize(savingStrategy, applicationLevelLog.eventQueue, applicationLevelLog.currentLogfile);
        }
        return applicationLevelLog;
    }

    @Override
    public void handle(Event event) {
        eventQueue.add(event);
        logSaver.save();
    }

    @Override
    public Path initLogFile() {
        String workspaceLocation = LocationUtils.getWorkspaceLocation();
        String logDirLocation = workspaceLocation.concat(LOG_DIRECTORY);
        if (!DirUtils.isDirectoryExists(logDirLocation)) {
            DirUtils.directoryCreate(logDirLocation);
        }
        this.logDaemon = logDaemonInvoke();
        return FileUtils.fileSave(logDirLocation.concat(LOGFILE_PREFIX.concat(LocalDate.now().toString())), "", true);
    }

    @Override
    public Path refreshLogFile() {
        if (FileUtils.isExist(LOGFILE_PREFIX.concat(LocalDate.now().toString()))) {
            return this.currentLogfile;
        }
        return initLogFile();
    }

    @Override
    public Path shutdownLogManager() {
        if (this.logDaemon != null) {
            this.logDaemon.cancel();
            logSaver.onShutdown()
                    .run();
        }
        return this.currentLogfile;
    }

    private Timer logDaemonInvoke() {
        if (this.logDaemon != null) {
            return this.logDaemon;
        } else {
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    initLogFile();
                }
            }, 5000, 5000);
            log.info("====> Started scheduler to follow log management");
            return timer;
        }
    }
}