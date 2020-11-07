package io.easeci.core.workspace.cache;

import java.nio.file.Path;

/**
 * Interface that exposing methods
 * for removing resource from cache of Easeci's workspace.
 * @author Karol Meksuła
 * 2020-11-07
 * */
public interface CacheGarbageCollector {

    /**
     * Removes concrete path of file or directory placed in .cache/ in workspace.
     * @param path is a path to file or directory.
     *             Path must be secured for removing only resources
     *             from workspace and not from outside of it
     * @return long value that inform us about deleted resources size in bytes
     * */
    long cleanup(Path path);

    /**
     * Removes all files and directories placed in .cache/ in workspace.
     * @return long value that inform us about deleted resources size in bytes
     * */
    long cleanup();
}
