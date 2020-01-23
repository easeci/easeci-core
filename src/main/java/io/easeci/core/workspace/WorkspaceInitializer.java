package io.easeci.core.workspace;

import org.javatuples.Pair;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

/**
 * WorkspaceInitializer is object that initialize,
 * create and check integrity of currently existing workspace.
 * Workspace is main place where are storage files required to
 * working application. These file are for example: pipelines, configuration files, logs etc.
 * */
public interface WorkspaceInitializer {

    /**
     *  Initializes workspace in indicated path.
     * @param path is Path where resources should be initialized
     * @return Path where resources was initialized
     * @throws AccessDeniedException when
     * */
    Path initializeMainWorkspace(Path path) throws AccessDeniedException;

    /**
     * Checks if workspace is initialized correctly.
     * @return Pair<Boolean, Path> if workspace is correctly detected
     *         returns True as first object
     *         returns Path if first logic argument is True
     * @throws IllegalStateException when something went wrong:
     *         - process with specified PID has no privileges to read indicated directory
     *         - specified Path is malformed, or not exists
     * */
    Pair<Boolean, Path> checkMainWorkspace(Path path) throws IllegalStateException;
}
