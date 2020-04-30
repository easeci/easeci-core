package io.easeci.core.bootstrap;

import io.easeci.core.extension.ExtensionSystem;
import io.easeci.core.log.ApplicationLevelLog;
import io.easeci.core.log.LogManager;
import io.easeci.core.output.Event;
import io.easeci.core.output.EventType;
import io.easeci.core.workspace.LinuxWorkspaceInitializer;
import io.easeci.core.workspace.WorkspaceGuard;
import io.easeci.core.workspace.WorkspaceInitializer;
import org.javatuples.Triplet;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static io.easeci.core.log.Publishers.SYSTEM;
import static java.util.Objects.isNull;

public class LinuxBootstrapper implements Bootstrapper {
    private static Bootstrapper bootstrapper;
    private static WorkspaceInitializer workspaceInitializer;
    private LogManager logManager;
    private ExtensionSystem extensionSystem;

    private LinuxBootstrapper() {}

    public static Bootstrapper getInstance() {
        if (isNull(bootstrapper)) {
            bootstrapper = new LinuxBootstrapper();
        }
        return bootstrapper;
    }

    @Override
    public void bootstrap(String[] args) {
        LinuxBootstrapper.workspaceInitializer = LinuxWorkspaceInitializer.getInstance();
        Path workspacePath = workspaceInitializer.init(args.length > 0 ? Optional.of(Path.of(args[0])) : Optional.empty());
        WorkspaceGuard workspaceGuard = (WorkspaceGuard) workspaceInitializer;
        Triplet<Boolean, Path, Set<String>> scanResult = workspaceGuard.scan(workspacePath);
        if (!scanResult.getValue0()) {
            workspaceGuard.fix(scanResult.getValue1());
        }
        this.logManager = ApplicationLevelLog.getInstance();
        this.logManager.handle(Event.builder()
                .eventMeta(Event.EventMeta.builder()
                        .eventType(EventType.RUNTIME)
                        .title("EaseCI is running")
                        .publishTimestamp(LocalDateTime.now())
                        .publishedBy(SYSTEM.name())
                        .build())
                .content("EaseCI core server correctly started.")
                .build());
        this.extensionSystem = ExtensionSystem.getInstance();
        this.extensionSystem.start();
        this.extensionSystem.startStandalonePlugins();
    }
}
