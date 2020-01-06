package io.easeci.utils.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    private final static String FILE_PATH = "/tmp/example.file";
    private final static String UNRECOGNIZED_PATH = "/tmp/not/existing/path";

    @Test
    @DisplayName("Should load file with success as a String")
    void loadFileTest() {
        final String FILE_CONTENT = Utils.ymlContent();

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);
        String contentLoaded = FileUtils.fileLoad(path.toString());

        assertEquals(FILE_CONTENT, contentLoaded);
    }

    @Test
    @DisplayName("Should not load file because not exist")
    void loadFileFailTest() {
        assertThrows(RuntimeException.class, () -> FileUtils.fileLoad(UNRECOGNIZED_PATH));
    }

    @Test
    @DisplayName("Should create file with specified content if file not exist - append arg is ignored")
    void fileSaveTest() {
        final String FILE_CONTENT = Utils.ymlContent();

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);
        boolean isFileExist = FileUtils.isExist(FILE_PATH);

        assertTrue(isFileExist);
        assertEquals(path.toString(), FILE_PATH);
    }

    @Test
    @DisplayName("Should append content to file if file exists - append arg is 'true'")
    void fileSaveAndAppendContentTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        final String ROW_APPENDED = Utils.ymlRow();
        final boolean APPEND = true;

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, APPEND);
        boolean isFileExist = FileUtils.isExist(FILE_PATH);

        FileUtils.fileSave(FILE_PATH, ROW_APPENDED, APPEND);

        assertAll(() -> assertEquals(FILE_CONTENT.concat(ROW_APPENDED), Files.readString(path)),
                () -> assertTrue(isFileExist),
                () -> assertEquals(path.toString(), FILE_PATH));
    }

    @Test
    @DisplayName("Should not append content to file if file exist but append arg is 'false'")
    void fileSaveAndNotAppendContentTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        final String ROW_APPENDED = Utils.ymlRow();
        final boolean APPEND = false;

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, APPEND);
        boolean isFileExist = FileUtils.isExist(FILE_PATH);

        assertAll(() -> assertThrows(RuntimeException.class, () -> FileUtils.fileSave(FILE_PATH, ROW_APPENDED, APPEND)),
                () -> assertTrue(isFileExist),
                () -> assertEquals(path.toString(), FILE_PATH),
                () -> assertEquals(FILE_CONTENT, Files.readString(path)),
                () -> assertNotEquals(FILE_CONTENT.concat(ROW_APPENDED), Files.readString(path)));
    }

    @Test
    @DisplayName("Should throw exception when trying to create file in directory that not exists")
    void fileSaveAndDirectoryNotExistTest() {
        final String FILE_CONTENT = Utils.ymlContent();

        assertThrows(RuntimeException.class, () -> FileUtils.fileSave(UNRECOGNIZED_PATH, FILE_CONTENT, false));
    }

    @Test
    @DisplayName("Should return false if file not exists")
    void isExistTest() {
        final String NOT_EXISTING_PATH = "/tmp/notexisting/file";

        boolean isFileExist = FileUtils.isExist(NOT_EXISTING_PATH);

        assertFalse(isFileExist);
    }

    @Test
    @DisplayName("Should delete file with success")
    void fileDeleteTest() {
        final String FILE_CONTENT = Utils.ymlContent();

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);
        boolean isRemoved = FileUtils.fileDelete(path.toString());

        assertAll(() -> assertTrue(isRemoved),
                () -> assertFalse(FileUtils.isExist(path.toString())));
    }

    @Test
    @DisplayName("Should not delete file that not exists")
    void notExistingFileDelete() {
        boolean isRemoved = FileUtils.fileDelete(UNRECOGNIZED_PATH);

        assertAll(() -> assertFalse(isRemoved),
                () -> assertFalse(FileUtils.isExist(UNRECOGNIZED_PATH)));
    }

    @Test
    void fileChangeTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        final String NEW_FILE_CONTENT = Utils.ymlContentUpdated();

        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);
        Path pathNew = FileUtils.fileChange(FILE_PATH, NEW_FILE_CONTENT);

        assertAll(() -> assertEquals(path, pathNew),
                () -> assertEquals(FileUtils.fileLoad(path.toString()), NEW_FILE_CONTENT));
    }

    @Test
    void fileChangeFailureTest() {
        final String NEW_FILE_CONTENT = Utils.ymlContentUpdated();

        assertThrows(RuntimeException.class, () -> FileUtils.fileChange(FILE_PATH, NEW_FILE_CONTENT));
    }

    @AfterEach
    void cleanup() {
        try {
            Files.deleteIfExists(Path.of(FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}