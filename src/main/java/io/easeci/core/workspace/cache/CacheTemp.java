package io.easeci.core.workspace.cache;

import java.nio.file.Path;

/**
 * Interface that exposing one method for save
 * temporary file in hard coded location
 * @author Karol Meksuła
 * 2020-11-07
 * */
public interface CacheTemp {

    /**
     * Save content in temporary file.
     * @param value bytes value to save in file
     * @return path that points to temporary file where content is written
     * */
    Path save(byte[] value);

    /**
     * Save content in temporary file.
     * @param value String value to save in file
     * @return path that points to temporary file where content is written
     * */
    Path save(String value);
}
