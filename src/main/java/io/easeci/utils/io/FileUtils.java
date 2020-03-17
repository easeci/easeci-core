package io.easeci.utils.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;

/**
 * This static utilities methods are custom wrapper for IO operations.
 * It was created in order to catch exception and make developers
 * calm down and not worrying about exception handling etc.
 * @author Karol Meksuła
 * 2019-01-10
 * */

@Slf4j
public class FileUtils {

    /**
     * This method loads file, read and return as String object
     * @param path is a String representation of file's location
     * @return String representation of file
     * @exception RuntimeException If file not exist, process has no privileges etc.
     * */
    public static String fileLoad(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load file from path: " + path);
        }
    }

    /**
     * This method saves file on local storage device
     * Creates new one if not exists or appends/updates content to existing file.
     * @param path is a String representation of file's location
     * @param content is a String representation of file's content to save od hard drive
     * @param append specifies if file could be modified.
     *               If is `true` file can be modifying
     *               If is `false` file cannot be modifying
     * @return a path to resource that was saved in execution of this function
     * @exception RuntimeException If file just exist on hard drive, and @param append
     *               is not set as `true`
     * @exception RuntimeException can be throwing when process has no privileges to
     *               write operation on indicated file
     * */
    public static Path fileSave(String path, String content, boolean append) {
        final boolean exists = isExist(path);
        if (!exists) {
            try {
                return Files.write(Path.of(path), content.getBytes(), StandardOpenOption.CREATE_NEW);
            } catch (FileAlreadyExistsException e) {
                log.info("File just exists: {}", path);
                return Paths.get(path);
            } catch (IOException e) {
                throw new RuntimeException("Directory not exist or you cannot permission to write files here: " + path);
            }
        } else {
            if (!append) {
                throw new RuntimeException("File [" + path + "] just exist!\n" +
                        "Set append argument to 'true' if you want to append/override file");
            } else {
                try {
                    return Files.write(Path.of(path), content.getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException("Could not save file because unrecognized error occurred.");
    }

    /**
     * This method deletes file from the local hard drive
     * @param path is a String representation of directory or file to delete
     * @return boolean value. If directory or file was removed returns `true`
     *                        If directory or file was not removed returns `false`
     * */
    public static boolean fileDelete(String path) {
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot remove file: " + path);
        }
    }

    /**
     * This method changes file's content for another
     * @param path is a String representation of path indicates to file that
     *             is designated to file's content change
     * @param contentNew is a String representation of new file's content that
     *                   will replace old file's content
     * @return path to file that was changes on local hard drive
     * @exception RuntimeException throws when file not exists
     *  */
    public static Path fileChange(String path, String contentNew) {
        boolean removed = fileDelete(path);
        if (removed) {
            return fileSave(path, contentNew, false);
        }
        throw new RuntimeException("Cannot update content of file because not exists!");
    }

    /**
     * This method simple wraps standard lib's method. Returns boolean value.
     * @param path path expressed in String that we want to check if exists
     * @return a boolean value. If file exists return true and if not exists
     * returns false
     * */
    public static boolean isExist(String path) {
        return Files.exists(Paths.get(path));
    }
}
