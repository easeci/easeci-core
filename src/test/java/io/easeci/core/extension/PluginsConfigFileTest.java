package io.easeci.core.extension;

import commons.WorkspaceTestUtils;
import io.easeci.utils.io.YamlUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginsConfigFileTest {
    private final static String FILE_CORRECT = "workspace/plugins-config-test.yml",
                              FILE_INCORRECT = "workspace/plugins-config-test-invalid.yml",
                                  FILE_EMPTY = "workspace/plugins-config-test-empty.yml";

    @Test
    @DisplayName("Should correctly create PluginConfigFile and correctly parse provided map")
    void instantiateAndParseTest() {
        Map<?, ?> values = WorkspaceTestUtils.loadYamlFromResources(FILE_CORRECT);
        PluginsConfigFile pluginsConfigFile = PluginsConfigFile.of(values);

        int size = pluginsConfigFile.getConfigDescriptions().size();

        List<ConfigDescription> configDescriptionListA = pluginsConfigFile.getConfigDescriptions().get("io.easeci.extension.bootstrap.OnStartup");
        List<ConfigDescription> configDescriptionListB = pluginsConfigFile.getConfigDescriptions().get("io.easeci.extension.bootstrap.TestPlugin");

        assertEquals(2, size);
        assertEquals(2, configDescriptionListA.size());
        assertEquals(2, configDescriptionListB.size());
    }

    @Test
    @DisplayName("Should not throwing and not return null pointer when trying to load empty config from plugins-config.yml file")
    void loadConfigFromFileTest() {
        Map<?, ?> values = WorkspaceTestUtils.loadYamlFromResources(FILE_EMPTY);
        PluginsConfigFile pluginsConfigFile = PluginsConfigFile.of(values);

        assertDoesNotThrow((ThrowingSupplier<NullPointerException>) NullPointerException::new);
        assertNotNull(pluginsConfigFile);
    }

    @Test
    @DisplayName("Should not throw NullPointerException when method's argument is null, should throw YamlException")
    void loadConfigWithNullTest() {
        assertThrows(YamlUtils.YamlException.class, () -> PluginsConfigFile.of(null));
    }

    @Test
    @DisplayName("Should throw ClassCastException when data in file is malformed (not expected types)")
    void loadConfigFromMalformedFileTest() {
        Map<?, ?> values = WorkspaceTestUtils.loadYamlFromResources(FILE_INCORRECT);

        assertThrows(ClassCastException.class, () -> PluginsConfigFile.of(values));
    }
}