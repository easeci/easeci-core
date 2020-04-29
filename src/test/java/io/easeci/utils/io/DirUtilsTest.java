package io.easeci.utils.io;

import io.easeci.commons.DirUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DirUtilsTest {
    private final static String DIR_PATH = "/tmp/test_dir",
                      CANNOT_ACCESS_PATH = "/root/test_dir",
                      RECURSIVE_DIR_PATH = DIR_PATH + "/child_dir",
                        COPY_TARGET_PATH = "/tmp/copy/dirs",
                       NOT_EXISTING_PATH = "/tmp/not/existing/path";

    @Test
    @DisplayName("Should create directory directly in existing directory")
    void directoryCreateTest() {
        Path path = DirUtils.directoryCreate(DIR_PATH);

        assertNotNull(path);
        assertEquals(DIR_PATH, path.toString());
        assertTrue(Files.isDirectory(Path.of(DIR_PATH)));
    }

    @Test
    @DisplayName("Should not create directory because just exists - should return same Path")
    void directoryCreateJustExistsTest() {
        DirUtils.directoryCreate(DIR_PATH);

        Path path = DirUtils.directoryCreate(DIR_PATH);

        assertEquals(DIR_PATH, path.toString());
        assertTrue(Files.isDirectory(path));
    }

    @Test
    @DisplayName("Should not create directory because just exists - should return same Path")
    void directoryCreatePermissionDeniedTest() {
        assertThrows(RuntimeException.class, () -> DirUtils.directoryCreate(CANNOT_ACCESS_PATH));
    }

    @Test
    @DisplayName("Should correctly create directory with parents, recursively")
    void directoryCreateParentDirNotExistsTest() throws IOException {
        Path path = DirUtils.directoryCreate(RECURSIVE_DIR_PATH);

        assertNotNull(path);
        assertEquals(RECURSIVE_DIR_PATH, path.toString());
        assertTrue(Files.isDirectory(Path.of(RECURSIVE_DIR_PATH)));

        Files.deleteIfExists(Path.of(RECURSIVE_DIR_PATH));
    }

    @Test
    @DisplayName("Should copy complete directory content to another place")
    void directoryRecursiveCopyTest() {
        Path pathNested = DirUtils.directoryCreate(RECURSIVE_DIR_PATH);

        Path path1 = Utils.saveSampleFile(Path.of(pathNested.toString().concat("/test_file.txt")));
        Path path2 = Utils.saveSampleFile(Path.of(DIR_PATH.concat("/test_file.txt")));

        Path path = DirUtils.directoryCopy(DIR_PATH, COPY_TARGET_PATH);

        assertTrue(Files.exists(path1));
        assertTrue(Files.exists(path2));
        assertEquals(COPY_TARGET_PATH, path.toString());
        assertTrue(Files.isDirectory(Paths.get(COPY_TARGET_PATH + "/child_dir")));
        assertTrue(Files.exists(Paths.get(COPY_TARGET_PATH + "/child_dir/test_file.txt")));
        assertTrue(Files.exists(Paths.get(COPY_TARGET_PATH + "/test_file.txt")));

        DirUtils.directoryDelete(DIR_PATH, true);
        DirUtils.directoryDelete(COPY_TARGET_PATH, true);
    }

    @Test
    @DisplayName("Should throw exception when we trying to copy files from not existing place")
    void notExistingDirectoryCopyTest() {
        assertThrows(RuntimeException.class, () -> DirUtils.directoryCopy(NOT_EXISTING_PATH, COPY_TARGET_PATH));
    }

    @Test
    @DisplayName("Should correctly return `true` if directory exists")
    void directoryExistsTest() {
        Path path = DirUtils.directoryCreate(DIR_PATH);

        assertTrue(DirUtils.isDirectoryExists(path.toString()));
    }

    @Test
    @DisplayName("Should correctly return `false` if directory not exists")
    void directoryNotExistsTest() {
        assertFalse(DirUtils.isDirectoryExists(DIR_PATH));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get(DIR_PATH));
    }
}