package io.easeci.core.workspace.easefiles;

import io.vavr.Tuple2;
import io.vavr.Tuple3;

import java.nio.file.Path;

/**
 * Interface to deal with directories to storage
 * and keep in clean hierarchy Easefiles.
 * @author Karol Meksuła
 * 2020-10-16
 * */
public interface DirectoriesIO {

    /**
     * Use this method to create directory.
     * @param path is a path where directory should be created.
     * @return path pointing where directory was just created.
     *          Second parameters indicates:
     *              true - when directory was created
     *              false - when directory was not created correctly
     *          Third parameter is error message
     * */
    Tuple3<Path, Boolean, String> createDirectory(Path path);

    /**
     * Use this method to remove directory
     * @param path is a path where directory should be created.
     * @param force is a flag that indicates to remove directory with content.
     *              If flag is true, method will remove directory with content,
     *              If flag is false, method will not remove directory
     * @return boolean value
     *            true - when directory was removed
     *            false - when directory was not removed correctly
     *        Second parameter is error message
     * */
    Tuple2<Boolean, String> deleteDirectory(Path path, boolean force);
}
