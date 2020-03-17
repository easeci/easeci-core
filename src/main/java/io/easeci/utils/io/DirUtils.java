package io.easeci.utils.io;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

/**
 * This static utilities methods are custom wrapper for IO operations.
 * It was created in order to catch exception and make developers
 * calm down and not worrying about exception handling etc.
 * @author Karol Meksuła
 * 2019-01-11
 * */

@Slf4j
public class DirUtils {

    /**
     * This method creates directory from path expressed as a String.
     * @param path is a path to the new directory
     * @return a path to the just created directory.
     * Throws exception when JVM process cannot privileges to IO operation
     *      in specific location.
     * */
    public static Path directoryCreate(String path) {
        try {
            return Files.createDirectories(Path.of(path));
        }
        catch (FileAlreadyExistsException e) {
            log.info("Directory just exists: {}", path);
            return Paths.get(path);
        } catch (IOException e) {
            log.error("Cannot create directory, probably runtime has no privileges to create directory," +
                    "or root dir not exists");
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * This method copies full content of one location to another recursively.
     * @param pathFrom is an path of origin, resources that are assigned to copy
     * @param pathTo is a target of coping operation
     * @return Path object that indicates where just copied resources are correctly placed
     * */
    public static Path directoryCopy(String pathFrom, String pathTo) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(new File(pathFrom), new File(pathTo));
            return Path.of(pathTo);
        } catch (IOException e) {
            throw new RuntimeException("Could not copy directory from " + pathFrom + " to " + pathFrom + ".\n" +
                    "Error caused by " + e.getMessage());
        }
    }

    /**
     * This method deletes directory, way of deletion is defined by `force` flag.
     * @param path is an path of resources to permanent removal
     * @param force is an flag that indicates if resources could be delete recursively or not
     *              If is true, resource will removed will full content, nested directories etc.
     *              If is false, resource will not removed
     * @return a path that indicates to resources that was attempted to remove
     * */
    public static Path directoryDelete(String path, boolean force) {
        final Path PATH = Path.of(path);
        try {
            if (force) {
                boolean isRemoved = FileSystemUtils.deleteRecursively(PATH);
                if (isRemoved) {
                    return PATH;
                }
                throw new FileNotFoundException("Cannot find directory to remove here: " + path);
            }
            Files.deleteIfExists(PATH);
            return PATH;
        } catch (DirectoryNotEmptyException e) {
            log.error("Directory " + path + " is not empty! If you want to delete force this by set `force` argument as `true`");
        } catch (IOException e) {
            log.error("Cannot delete directory: " + e.getCause());
        }
        return PATH;
    }

    /**
     * This method simple wraps standard lib's method. Returns boolean value.
     * @param path path expressed in String that we want to check if exists
     * @return a boolean value. If directory exists return true and if not exists
     * returns false
     * */
    public static boolean isDirectoryExists(String path) {
        return Files.isDirectory(Path.of(path));
    }
}
