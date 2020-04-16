package io.easeci.core.extension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginConfigTest {
    private final static String PLUGIN_CONFIG_FILE = "workspace/plugins-config-test.json",
                                 NOT_EXISTING_FILE = "workspace/not-exists/plugins-config-test.json",
                               INVALID_CONFIG_FILE = "workspace/plugins-config-test-invalid.json";

    private final static String INSTANCE_A_INTERFACE = "io.easeci.extension.bootstrap.OnStartup",
                                INSTANCE_B_INTERFACE = "io.easeci.extension.bootstrap.OnStartup";

    private final static String INSTANCE_A_OBJECT = "This is an A instance",
                                INSTANCE_B_OBJECT = "This is an B instance";

    @Test
    @DisplayName("Should correctly instantiate object")
    void instantiateTest() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);

        assertDoesNotThrow(() -> new DefaultPluginConfig(path));
    }

    @Test
    @DisplayName("Should not correctly instantiate object because file not exists")
    void instantiateFailTest() {
        assertThrows(IllegalStateException.class, () -> new DefaultPluginConfig(Paths.get(NOT_EXISTING_FILE)));
    }

    @Test
    @DisplayName("Should not correctly instantiate object because path is null")
    void instantiateFailNullTest() {
        assertThrows(IllegalStateException.class, () -> new DefaultPluginConfig(null));
    }

    @Test
    @DisplayName("Should correctly load config from file indicated as a method argument")
    void loadTest() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        PluginsConfigFile pluginsConfigFile = pluginConfig.load();

        assertAll(() -> assertNotNull(pluginsConfigFile),
                () -> assertNotNull(pluginsConfigFile.getConfigDescriptions()),
                () -> assertEquals(2, pluginsConfigFile.getConfigDescriptions().size()));
    }

    @Test
    @DisplayName("Should return null when exception occurred while config file parsing")
    void loadReturnNull() {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        PluginsConfigFile pluginsConfigFile = pluginConfig.load();

        assertNull(pluginsConfigFile);
    }

    @Test
    @DisplayName("Should return null when one of method argument is null in choose()")
    void loadReturnNullArgumentNull() {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        Instance chosenInstance = pluginStrategy.choose(Collections.emptyList(), null);

        assertNull(chosenInstance);
    }

    @Test
    @DisplayName("Should return null when list in method argument is empty")
    void loadReturnNullEmptyList() {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        Instance chosenInstance = pluginStrategy.choose(Collections.emptyList(), "io.easeci.extension.bootstrap.OnStartup");

        assertNull(chosenInstance);
    }

    @Test
    @DisplayName("Should return one and only instance when this instance is on list in method argument")
    void loadReturnOneInstanceOnList() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        List<Instance> instances = provideSingleInstanceList();
        Instance chosen = pluginStrategy.choose(instances, INSTANCE_A_INTERFACE);

        Object text = chosen.getInstance();

        assertEquals(INSTANCE_A_OBJECT, text);
    }

    /*
    * Tests of this functionality are not authoritative. Be careful not to be misled. Unfortunately, the choice of plug-in implementation should be tested manually.
    * */
    @Test
    @DisplayName("Should return correctly instance marked in field `Boolean enabled` as true")
    void loadReturnInstanceMarkedAsTrue() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        List<Instance> instances = provideMultiInstanceList();
        Instance chosen = pluginStrategy.choose(instances, INSTANCE_A_INTERFACE);

        Object text = chosen.getInstance();

        assertEquals(INSTANCE_A_OBJECT, text);
    }

    @Test
    @DisplayName("Should correctly save modified object to file in correct yaml format")
    void savePluginConfigurationTest() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);
        PluginsConfigFile before = pluginConfig.load();

//        Before adding new implementation size = 2
        assertEquals(2, before.getConfigDescriptions().get("io.easeci.extension.bootstrap.TestPlugin").size());

        ConfigDescription configDescription = ConfigDescription.builder()
                .uuid(UUID.randomUUID())
                .name("add-test-plugin")
                .version("0.0.1")
                .enabled(false)
                .build();

        pluginConfig.add("io.easeci.extension.bootstrap.TestPlugin", configDescription);
        PluginsConfigFile saved = pluginConfig.save();

//        Before adding new implementation size = 3
        assertAll(() -> assertNotNull(saved),
                () -> assertEquals(3, saved.getConfigDescriptions().get("io.easeci.extension.bootstrap.TestPlugin").size()));
    }

    private List<Instance> provideSingleInstanceList() {
        try {
            return new ArrayList<>(Collections.singletonList(Instance.builder()
                    .instance(INSTANCE_A_OBJECT)
                    .plugin(Plugin.of(
                            Plugin.of("welcome-logo", "0.0.1"),
                            Plugin.JarArchive.of(
                                    "filename",
                                    true,
                                    new URL("http://"),
                                    Path.of(""),
                                    ExtensionManifest.of(
                                            INSTANCE_A_INTERFACE,
                                            ""
                                    ))
                    ))
                    .build()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Instance> provideMultiInstanceList() {
        List<Instance> instanceList = provideSingleInstanceList();

        try {
            instanceList.add(Instance.builder()
                    .instance(INSTANCE_B_OBJECT)
                    .plugin(Plugin.of(
                            Plugin.of("welcome-logo", "0.0.1"),
                            Plugin.JarArchive.of(
                                    "filename",
                                    true,
                                    new URL("http://"),
                                    Path.of(""),
                                    ExtensionManifest.of(
                                            INSTANCE_A_INTERFACE,
                                            ""
                                    ))
                    ))
                    .build());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return instanceList;
    }
}