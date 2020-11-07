package io.easeci.core.workspace.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.workspace.LocationUtils.getCacheDirectoryLocation;
import static org.junit.jupiter.api.Assertions.*;

class CacheManagerTest {

    @Test
    @DisplayName("Should clean cache directory in workspace")
    void shouldCleanCacheTest() throws IOException {
        Path path = getCacheDirectoryLocation();
        Path fileToCreate = Paths.get(path.toString().concat("/test-file"));
        Path directoryToCreate = Paths.get(path.toString().concat("/test-dir"));

        Files.createFile(fileToCreate);
        Files.createDirectory(directoryToCreate);

        CacheManager cacheManager = CacheManager.getInstance();
        long bytesRemoved = cacheManager.cleanup();

        assertEquals(4096, bytesRemoved);
    }

    @Test
    @DisplayName("Should clean cache in concrete path")
    void shouldCleanConcretePathCacheTest() throws IOException {
        Path path = getCacheDirectoryLocation();
        Path directoryToCreate = Paths.get(path.toString().concat("/test-dir"));
        Path fileToCreate = Paths.get(directoryToCreate.toString().concat("/test-file"));
        final String text = "Some text value";

        if (!Files.exists(directoryToCreate)) {
            Files.createDirectory(directoryToCreate);
        }

        Files.createFile(fileToCreate);
        Files.writeString(fileToCreate, text);

        CacheManager cacheManager = CacheManager.getInstance();
        long bytesRemoved = cacheManager.cleanup(directoryToCreate);

        assertEquals(text.getBytes().length, bytesRemoved);
    }

    @AfterEach
    void cleanup() {
        CacheManager.getInstance().cleanup();
    }
}