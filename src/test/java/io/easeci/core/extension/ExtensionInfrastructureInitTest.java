package io.easeci.core.extension;

import io.easeci.core.bootstrap.LinuxBootstrapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.easeci.core.workspace.LocationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionInfrastructureInitTest {

    @BeforeAll
    static void setup() {
        LinuxBootstrapper.getInstance().bootstrap(new String[] {});
    }

    @Test
    @DisplayName("Should correctly initialize ExtensionInfrastructureInit object")
    void extensionInfrastructureInitTest() {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();

        assertNotNull(extensionInfrastructureInit);
        assertEquals(2, extensionInfrastructureInit.getPluginDirectories().size());
    }

    @Test
    @DisplayName("")
    void extensionInfrastructureInitIsInitializedTest() {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.isInitialized();
    }

    @AfterAll
    static void cleanup() throws IOException {
        String workspaceLocation = getWorkspaceLocation();
        File runFile = getRunFile();

        FileUtils.deleteDirectory(Paths.get(workspaceLocation).toFile());
        Files.deleteIfExists(runFile.toPath());
    }
}