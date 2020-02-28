package io.easeci.core.log;

import io.easeci.core.log.file.LogSaver;
import io.easeci.core.output.Event;
import io.easeci.utils.io.DirUtils;
import io.easeci.utils.io.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.easeci.core.workspace.WorkspaceUtils.getWorkspaceLocation;
import static java.util.Objects.isNull;

@Slf4j
public class ApplicationLevelLog implements LogManager {
    private static final String LOGFILE_PREFIX = "easeci-logs-",
                                LOG_DIRECTORY = "/log/";
    private static ApplicationLevelLog applicationLevelLog;
    private Queue<Event> eventQueue;
    private LogTarget logTarget;
    private LogSaver logSaver;
    private Thread logDaemon;
    private Path currentLogfile;

    private ApplicationLevelLog() {
        this.currentLogfile = initLogFile();
        this.eventQueue = new ConcurrentLinkedQueue<>();
    }

    public static ApplicationLevelLog getInstance() {
        if (isNull(applicationLevelLog)) {
            ApplicationLevelLog.applicationLevelLog = new ApplicationLevelLog();
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
        String workspaceLocation = getWorkspaceLocation();
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

    private Thread logDaemonInvoke() {
        if (this.logDaemon != null && this.logDaemon.isAlive()) {
            return this.logDaemon;
        } else {
            Thread logDaemon = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        initLogFile();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            logDaemon.setDaemon(true);
            logDaemon.start();
            return logDaemon;
        }
    }
}
