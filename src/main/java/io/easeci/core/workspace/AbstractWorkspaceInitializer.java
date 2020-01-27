package io.easeci.core.workspace;

import org.javatuples.Pair;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

/**
 * Abstract implementation of WorkspaceInitializer.
 * Proxy class between abstract interface and concrete implementations.
 * Defines one template method to create workspace and some useful utils
 * methods helpful in workspace creation.
 *
 * */
abstract class AbstractWorkspaceInitializer implements WorkspaceInitializer {

    @Override
    public Path initializeMainWorkspace(Path path) throws IOException {
        validatePath(path);
        Path workspace = copyConfig(path);
        return createRunYml(workspace, path);
    }

    @Override
    public Pair<Boolean, Path> checkMainWorkspace(Path path) {
        return null;
    }

    /**
     * Simply copy predefined, basic and default configuration to specified
     * directory provided on startup application. If .run.yml not exists in
     * @param path represents path where resources will be created
     * @return path where resources was just created
     * */
    abstract Path copyConfig(Path path);

    /**
     * Creates '.run.yml' file. This file is required to store path to workspace
     * in your local operating system. If EaseCI core workspace just exists in
     * your OS, you can put '.run.yml' file to directory with .jar archive with
     * EaseCI Core application.
     * @param path represents path where .run.yml file will be created
     * @param workspaceLocation represents path where resources was created and this path
     *             should be placed in .run.yml file.
     * @return Path where .run.yml file was created.
     * */
    abstract Path createRunYml(Path path,  Path workspaceLocation);

    /**
     * Validate path passed to method as argument. Checks is EaseCI is able to
     * create workspace directory in typed location.
     * @param path is path where resources with EaseCI workspace will be created.
     * @throws IOException when files could not be created at Path location
     *          because process has no permission to IO operation or specified
     *          location just not exists.
     * */
    private void validatePath(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        if (!Files.isDirectory(path)) {
            throw new NotDirectoryException(path.toString() + " is not directory! Cannot create workspace!");
        }
        Path trialFile = Files.createFile(path);
        if (!Files.exists(trialFile)) {
            throw new AccessDeniedException("Cannot create file in location: " + path.toString());
        }
        Files.deleteIfExists(trialFile);
    }
}
