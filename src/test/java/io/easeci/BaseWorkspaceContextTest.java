package io.easeci;

import io.easeci.core.bootstrap.BootstrapperFactory;
import io.easeci.core.extension.PluginSystemCriticalException;
import io.easeci.core.log.ApplicationLevelLog;
import io.easeci.core.workspace.cache.CacheManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.easeci.core.workspace.LocationUtils.getRunFile;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;

public abstract class BaseWorkspaceContextTest {

    @BeforeAll
    public static void setup() {
        try {
            ApplicationLevelLog.destroyInstance();
            CacheManager.destroyInstance();
            BootstrapperFactory.factorize().bootstrap(new String[]{});
        } catch (PluginSystemCriticalException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void cleanup() throws IOException {
//        FileUtils.deleteDirectory(Path.of(getWorkspaceLocation()).toFile());
//        Files.deleteIfExists(getRunFile().toPath());
    }
}
