package io.easeci.core.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static org.junit.jupiter.api.Assertions.*;

class PluginsConfigFileTest {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String FILE_CORRECT = "workspace/plugins-config-test.json",
                              FILE_INCORRECT = "workspace/plugins-config-test-invalid.json",
                                  FILE_EMPTY = "workspace/plugins-config-test-empty.json";

    @Test
    @DisplayName("Should correctly create PluginConfigFile and correctly parse provided map")
    void instantiateAndParseTest() throws IOException {
        Path path = buildPathFromResources(FILE_CORRECT);
        PluginsConfigFile pluginsConfigFile = objectMapper.readValue(path.toFile(), PluginsConfigFile.class);

        int size = pluginsConfigFile.getConfigDescriptions().size();

        Set<ConfigDescription> configDescriptionListA = pluginsConfigFile.getConfigDescriptions().get("io.easeci.extension.bootstrap.OnStartup");
        Set<ConfigDescription> configDescriptionListB = pluginsConfigFile.getConfigDescriptions().get("io.easeci.extension.bootstrap.TestPlugin");

        assertEquals(2, size);
        assertEquals(3, configDescriptionListA.size());
        assertEquals(2, configDescriptionListB.size());
    }

    @Test
    @DisplayName("Should not throwing and not return null pointer when trying to load empty config from plugins-config.yml file")
    void loadConfigFromFileTest() throws IOException {
        Path path = buildPathFromResources(FILE_EMPTY);
        PluginsConfigFile pluginsConfigFile = objectMapper.readValue(path.toFile(), PluginsConfigFile.class);

        assertDoesNotThrow((ThrowingSupplier<NullPointerException>) NullPointerException::new);
        assertNotNull(pluginsConfigFile);
    }

}