package io.easeci.core.workspace;

import org.javatuples.Pair;

import java.nio.file.Path;

/**
 * Abstract implementation of WorkspaceInitializer.
 * Proxy class between abstract interface and concrete implementations.
 * Defines one template method to create workspace and some useful utils
 * methods helpful in workspace creation.
 * */
abstract class AbstractWorkspaceInitializer implements WorkspaceInitializer {

    @Override
    public Path initializeMainWorkspace(Path path) {
        return null;
    }

    @Override
    public Pair<Boolean, Path> checkMainWorkspace(Path path) {
        return null;
    }

    /**
     * Simply copy predefined, basic and default configuration to specified
     * directory provided on startup application. If .run.yml not exists in
     * */
    abstract Path copyConfig();

    /**
     * Creates '.run.yml' file. This file is required to store path to workspace
     * in your local operating system. If EaseCI core workspace just exists in
     * your OS, you can put '.run.yml' file to directory with .jar archive with
     * EaseCI Core application.
     * */
    private Path createRunYml() {
        return null;
    }
}
