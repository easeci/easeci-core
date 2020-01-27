package io.easeci;

import io.easeci.core.workspace.StandardWorkspaceInitializer;
import io.easeci.core.workspace.Workspace;
import io.easeci.interpreter.Python;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.Optional;

@SpringBootApplication
public class EaseciCoreApplication {
    private static Workspace workspace;

    public static void bootstrap(String[] args) {
//        Python.initializeInterpreter();
        EaseciCoreApplication.workspace = new StandardWorkspaceInitializer();
        Path workspacePath = workspace.init(args.length > 0 ? Optional.of(Path.of(args[0])) : Optional.empty());
        workspace.validate(workspacePath);
    }

    public static void main(String[] args) {
        bootstrap(args);
        SpringApplication.run(EaseciCoreApplication.class, args);
    }

}
