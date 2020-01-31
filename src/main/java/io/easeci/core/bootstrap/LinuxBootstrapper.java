package io.easeci.core.bootstrap;

import io.easeci.core.workspace.LinuxWorkspaceInitializer;
import io.easeci.core.workspace.WorkspaceGuard;
import io.easeci.core.workspace.WorkspaceInitializer;
import org.javatuples.Triplet;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;

public class LinuxBootstrapper implements Bootstrapper {
    private static Bootstrapper bootstrapper;
    private static WorkspaceInitializer workspaceInitializer;

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
    }
}
