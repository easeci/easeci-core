package io.easeci;

import io.easeci.core.workspace.LinuxWorkspaceInitializer;
import io.easeci.core.workspace.WorkspaceGuard;
import io.easeci.core.workspace.WorkspaceInitializer;
import io.easeci.interpreter.Python;
import org.javatuples.Triplet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class EaseciCoreApplication {
    private static WorkspaceInitializer workspace;

    public static void bootstrap(String[] args) {
//        Python.initializeInterpreter();
        EaseciCoreApplication.workspace = LinuxWorkspaceInitializer.getInstance();
        Path workspacePath = workspace.init(args.length > 0 ? Optional.of(Path.of(args[0])) : Optional.empty());
        WorkspaceGuard workspaceGuard = (WorkspaceGuard) workspace;
        Triplet<Boolean, Path, Set<String>> scanResult = workspaceGuard.scan(workspacePath);
        if (!scanResult.getValue0()) {
            workspaceGuard.fix(scanResult.getValue1());
        }
    }

    public static void main(String[] args) {
        bootstrap(args);
        SpringApplication.run(EaseciCoreApplication.class, args);
    }

}
