package io.easeci.core.workspace.easefiles;

import java.nio.file.Path;

/**
 * Interface to deal with Easefiles Input/Output operations.
 * @author Karol Meksuła
 * 2020-10-06
 * */
public interface EasefileIO {

    /**
     * @param path is a path where Easefile is placed on storage.
     * Use this method to read pipeline Easefile from storage.
     * @return EasefileOut that has status, errorMessage or file
     *         content as String representation of loaded Easefile
     * */
    EasefileOut load(Path path);

    /**
     * @param path is a path where Easefile is placed on storage.
     * @param easefileAsString is a content string representation of Easefile.
     * Use this method to read pipeline Easefile from storage.
     * @return EasefileOut that has status, errorMessage or file
     *         content as String representation of loaded Easefile
     * */
    EasefileOut save(Path path, String easefileAsString);

    /**
     * @param filename is a name of file where Easefile's content will placed.
     * @param easefileAsString is a content string representation of Easefile.
     * Use this method to read pipeline Easefile from storage.
     * @return EasefileOut that has status, errorMessage or file
     *         content as String representation of loaded Easefile
     * */
    EasefileOut save(String filename, String easefileAsString);

    /**
     * @param path is a path where Easefile is placed on storage.
     * @param easefileNewContent is a string representation of content to replace with.
     * Use this method to update pipeline Easefile existing
     * in storage and replace content with new one.
     * @return EasefileOut that has status, errorMessage or file
     *         content as String representation of loaded Easefile
     * */
    EasefileOut update(Path path, String easefileNewContent);

    /**
     * @param path is a path where Easefile is placed on storage.
     * Use this method to remove pipeline Easefile from storage.
     * @return true when file was removed or false when not removed.
     * */
    boolean delete(Path path);
}
