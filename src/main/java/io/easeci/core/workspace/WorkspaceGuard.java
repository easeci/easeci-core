package io.easeci.core.workspace;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

/**
 * WorkspaceInitializer is object that initialize,
 * create and check integrity of currently existing workspace.
 * Workspace is main place where are storage files required to
 * working application. These file are for example: pipelines, configuration files, logs etc.
 * */
public interface WorkspaceGuard {

    /**
     * Checks if workspace is initialized correctly.
     * @param workspacePath is a path to resources location on local storage
     * @return Triplet<Boolean, Path> if workspace is correctly detected
     *         returns True as first object
     *         returns Path if first logic argument is True
     *         If some Files were not detected, these will be added to Set<String>
     * @throws IllegalStateException when something went wrong:
     *         - process with specified PID has no privileges to read indicated directory
     *         - specified Path is malformed, or not exists
     * */
    Triplet<Boolean, Path, Set<String>> scan(Path workspacePath) throws IllegalStateException;

    /**
     * Fixes an incomplete file system of workspace.
     * @param workspacePath is a path to resources location on local storage
     * @return Pair<Boolean, Set<File> so first argument in this tuple is boolean
     *          that is just a result of fixing operation. In Set<File> are collected
     *          all missing or files damaged before and repaired now.
     * */
    Pair<Boolean, Set<File>> fix(Path workspacePath);
}
