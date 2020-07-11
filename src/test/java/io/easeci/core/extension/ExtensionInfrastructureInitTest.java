package io.easeci.core.extension;

import io.easeci.core.bootstrap.LinuxBootstrapper;
import io.easeci.commons.DirUtils;
import io.easeci.commons.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.easeci.core.workspace.LocationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionInfrastructureInitTest {

    @BeforeAll
    static void setup() throws PluginSystemCriticalException {
        LinuxBootstrapper.getInstance().bootstrap(new String[] {});
    }

    @Test
    @DisplayName("Should correctly initialize ExtensionInfrastructureInit object")
    void extensionInfrastructureInitTest() {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();

        assertNotNull(extensionInfrastructureInit);
        assertEquals(0, extensionInfrastructureInit.getPluginDirectories().size());
    }

    @Test
    @DisplayName("Should correctly detect well-initialized plugin infrastructure")
    void extensionInfrastructureInitIsInitializedTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.prepareInfrastructure();

        boolean isInitialized = extensionInfrastructureInit.isInitialized();

        assertTrue(isInitialized);

        cleanDirs(extensionInfrastructureInit.getPluginDirectories());
    }

    @Test
    @DisplayName("Should detect that plugin infrastructure is not correctly created - plugins.yml not exists")
    void extensionInfrastructureInitIsInitializedFailTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.prepareInfrastructure();

        FileUtils.fileDelete(getPluginsYmlLocation().toString());
        boolean isInitialized = extensionInfrastructureInit.isInitialized();

        assertFalse(isInitialized);

        cleanDirs(extensionInfrastructureInit.getPluginDirectories());
    }

    @Test
    @DisplayName("Should detect that plugin infrastructure is not correctly created - one of plugins dirs not exists")
    void extensionInfrastructureInitIsInitializedFailDirectoryTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.prepareInfrastructure();

        DirUtils.directoryDelete(extensionInfrastructureInit.getPluginDirectories().get(0).toString(), true);
        boolean isInitialized = extensionInfrastructureInit.isInitialized();

        assertFalse(isInitialized);

        cleanDirs(extensionInfrastructureInit.getPluginDirectories());
    }

    @Test
    @DisplayName("Should correctly initialize locations for plugin system")
    void extensionInfrastructureInitPrepareTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();

        extensionInfrastructureInit.prepareInfrastructure();
        List<Path> pluginDirectories = extensionInfrastructureInit.getPluginDirectories();

        final int expectedSize = 3; // 2 directories + 1 plugin.yml.file

        assertAll(() -> assertEquals(expectedSize, pluginDirectories.size()),
                () -> pluginDirectories.forEach(path -> assertTrue(Files.exists(path))));

        cleanDirs(pluginDirectories);
    }

    @Test
    @DisplayName("Should correctly load plugins infrastructure")
    void extensionInfrastructureInitLoadTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.prepareInfrastructure();
        extensionInfrastructureInit.loadInfrastructure();

        assertEquals(2, extensionInfrastructureInit.getPluginDirectories().size());

        List<String> pathsToCreation = List.of("1", "2", "3");
        Path pluginsYmlLocation = getPluginsYmlLocation();
        FileUtils.fileDelete(pluginsYmlLocation.toString());
        extensionInfrastructureInit.createMinimalisticPluginYml(pluginsYmlLocation, pathsToCreation);

        extensionInfrastructureInit.loadInfrastructure();

        assertAll(() -> assertEquals(3, extensionInfrastructureInit.getPluginDirectories().size()));
    }

    @Test
    @DisplayName("Should throw exception when trying to invoke loadInfrastructure() without plugins.yml file")
    void extensionInfrastructureInitLoadNoPluginYmlTest() {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();

        FileUtils.fileDelete(getPluginsYmlLocation().toString());

        assertThrows(Exception.class, extensionInfrastructureInit::loadInfrastructure);
    }

    @Test
    @DisplayName("Should correctly add to plugin dirs when trying to use not allowed placeholder in variable")
    void extensionInfrastructureInitLoadNoPlaceholderYmlTest() throws Exception {
        ExtensionInfrastructureInit extensionInfrastructureInit = new ExtensionInfrastructureInit();
        extensionInfrastructureInit.prepareInfrastructure();

        List<String> pathsToCreation = List.of("<unrecognized>/plugins");
        Path pluginsYmlLocation = getPluginsYmlLocation();
        FileUtils.fileDelete(pluginsYmlLocation.toString());
        extensionInfrastructureInit.createMinimalisticPluginYml(pluginsYmlLocation, pathsToCreation);

        extensionInfrastructureInit.loadInfrastructure();

        assertEquals(1, extensionInfrastructureInit.getPluginDirectories().size());
    }

    @AfterEach
    void cleanEach() {
        FileUtils.fileDelete(getPluginsYmlLocation().toString());
    }

    @AfterAll
    static void cleanup() throws IOException {
        String workspaceLocation = getWorkspaceLocation();
        File runFile = getRunFile();

        DirUtils.directoryDelete(workspaceLocation, true);
        Files.deleteIfExists(runFile.toPath());
    }

    void cleanDirs(List<Path> dirs) {
        dirs.forEach(path -> DirUtils.directoryDelete(path.toString(), true));
    }
}