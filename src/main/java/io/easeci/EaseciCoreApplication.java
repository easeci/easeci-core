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

    public static void prepare(String[] args) {
        Python.initializeInterpreter();
        EaseciCoreApplication.workspace = new StandardWorkspaceInitializer();
        workspace.init(args.length > 0 ? Optional.of(Path.of(args[0])) : Optional.empty());
    }

    public static void main(String[] args) {
        prepare(args);
        SpringApplication.run(EaseciCoreApplication.class, args);
    }

}
