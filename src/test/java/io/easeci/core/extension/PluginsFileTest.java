package io.easeci.core.extension;

import io.easeci.core.bootstrap.LinuxBootstrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class PluginsFileTest {

    @BeforeAll
    static void setup() {
        LinuxBootstrapper.getInstance().bootstrap(new String[]{});
    }

    @Test
    void parsePluginsFileToObject() {
        Path path = getPluginsYmlLocation();

        PluginsFile pluginsFile = PluginsFile.create(path);

        assertAll(() -> assertNotNull(pluginsFile),
                () -> assertNotNull(pluginsFile.getPluginsList()),
                () -> assertNotNull(pluginsFile.getRegistryTimeout()),
                () -> assertNotNull(pluginsFile.getRegistryUrl()));
    }
}