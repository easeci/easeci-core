package io.easeci.utils.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileUtils {

    public static String fileLoad(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load file from path: " + path);
        }
    }

    public static Path fileSave(String path, String content, boolean append) {
        final boolean exists = isExist(path);
        if (!exists) {
            try {
                return Files.write(Path.of(path), content.getBytes(), StandardOpenOption.CREATE_NEW);
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

    public static boolean fileDelete(String path) {
        try {
            return Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot remove file: " + path);
        }
    }

    public static Path fileChange(String path, String contentNew) {
        boolean removed = fileDelete(path);
        if (removed) {
            return fileSave(path, contentNew, false);
        }
        throw new RuntimeException("Cannot update content of file because not exists!");
    }

    public static boolean isExist(final String PATH) {
        return Files.exists(Paths.get(PATH));
    }

}
