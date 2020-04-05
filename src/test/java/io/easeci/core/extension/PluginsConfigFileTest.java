package io.easeci.core.extension;

import commons.WorkspaceTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginsConfigFileTest {
    private final static String FILE_CORRECT = "workspace/plugins-config-test.yml",
                              FILE_INCORRECT = "workspace/plugins-config-test-invalid.yml";

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
}