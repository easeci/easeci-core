package io.easeci.core.workspace;

import java.nio.file.Path;

/**
 * Functional interface that has one role - checks if data
 * is consistent and EaseCI's file system is not damaged.
 * @author Karol Meksuła
 * 2020-01-27
 * */
public interface DataIntegrityValidator<T> {

    /**
     * Validate integration and consistence of data placed in some
     * path at your local or remote storage.
     * @param workspacePath is Path object that indicates where
     *                     resources intended for checking are placed.
     * @return T with a result of data integrity checking process.
     * */
    T validate(Path workspacePath);
}
