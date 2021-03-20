package io.easeci.core.extension;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.extension.ExtensionType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginConfigTest extends BaseWorkspaceContextTest {
    private final static String PLUGIN_CONFIG_FILE = "workspace/plugins-config-test-copy-2.json",
                                 NOT_EXISTING_FILE = "workspace/not-exists/plugins-config-test.json",
                               INVALID_CONFIG_FILE = "workspace/plugins-config-test-invalid.json",
           PLUGIN_CONFIG_FILE_WITH_NOT_UNIQUE_UUID = "workspace/plugins-config-test-not-unique-uuid.json";

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
    void loadTest() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        PluginsConfigFile pluginsConfigFile = pluginConfig.load();

        assertAll(() -> assertNotNull(pluginsConfigFile),
                () -> assertNotNull(pluginsConfigFile.getConfigDescriptions()),
                () -> assertEquals(2, pluginsConfigFile.getConfigDescriptions().size()));
    }

    @Test
    @DisplayName("Should return null when exception occurred while config file parsing")
    void loadReturnNull() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        PluginsConfigFile pluginsConfigFile = pluginConfig.load();

        assertNull(pluginsConfigFile);
    }

    @Test
    @DisplayName("Should return null when one of method argument is null in choose()")
    void loadReturnNullArgumentNull() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        Instance chosenInstance = pluginStrategy.choose(Collections.emptyList(), null);

        assertNull(chosenInstance);
    }

    @Test
    @DisplayName("Should return null when list in method argument is empty")
    void loadReturnNullEmptyList() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(INVALID_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        Instance chosenInstance = pluginStrategy.choose(Collections.emptyList(), "io.easeci.extension.bootstrap.OnStartup");

        assertNull(chosenInstance);
    }

    @Test
    @DisplayName("Should return one and only instance when this instance is on list in method argument")
    void loadReturnOneInstanceOnList() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        List<Instance> instances = provideSingleInstanceList();
        Instance chosen = pluginStrategy.choose(instances, INSTANCE_A_INTERFACE);

        Object text = chosen.getInstance();

        assertEquals(INSTANCE_A_OBJECT, text);
    }

    /*
    * Tests of this functionality are not authoritative. Be careful not to be misled.
    * Unfortunately, the choice of plug-in implementation should be tested manually.
    * */
    @Disabled
    @Test
    @DisplayName("Should return correctly instance marked in field `Boolean enabled` as true")
    void loadReturnInstanceMarkedAsTrue() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(path);

        List<Instance> instances = provideMultiInstanceList();
        Instance chosen = pluginStrategy.choose(instances, INSTANCE_A_INTERFACE);

        Object text = chosen.getInstance();

        assertEquals(INSTANCE_A_OBJECT, text);
    }

    @Test
    @DisplayName("Should correctly save modified object to file in correct yaml format")
    void savePluginConfigurationTest() throws PluginSystemCriticalException {
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

    @Test
    @DisplayName("Should correctly disable extension by pluginName and pluginVersion")
    void disableByPluginNameAndVersionTest() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        String pluginNameToDisable = "welcome-logo";
        String pluginVersionToDisable = "0.0.1";

        boolean isDisabled = pluginConfig.disable(pluginNameToDisable, pluginVersionToDisable);

//        check is configuration is reloaded to newest version after plugin disabling
        PluginStrategy pluginStrategy = (PluginStrategy) pluginConfig;
        ConfigDescription configDescription = pluginStrategy.find(ExtensionType.EXTENSION_PLUGIN, pluginNameToDisable, pluginVersionToDisable);

        assertAll(() -> assertTrue(isDisabled),
                () -> assertEquals(pluginNameToDisable, configDescription.getName()),
                () -> assertEquals(pluginVersionToDisable, configDescription.getVersion()),
                () -> assertFalse(configDescription.getEnabled()));
    }

    @Test
    @DisplayName("Should not disable extension if there is no such pluginName and pluginVersion")
    void disableByPluginNameAndVersionFailureTest() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginConfig pluginConfig = new DefaultPluginConfig(path);

        String pluginNameToDisable = "welcome-logo2";  // not exists in plugins-config.json
        String pluginVersionToDisable = "0.0.1";

        boolean isDisabled = pluginConfig.disable(pluginNameToDisable, pluginVersionToDisable);

        assertFalse(isDisabled);
    }

    @Test
    @DisplayName("Should correctly detect repetition of UUID in PluginsConfigFile.class and throw exception")
    void uniquePluginConfigCheckTest() {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE_WITH_NOT_UNIQUE_UUID);

//          constructor invokes load() method that next invokes function under testing uniquePluginConfigCheck()
        assertThrows(PluginSystemCriticalException.class, () -> new DefaultPluginConfig(path));
    }

    @Test
    @DisplayName("Should not detect any repetition of UUID in PluginsConfigFile.class and not trow any exception")
    void uniquePluginConfigCheckNotDetectTest() throws PluginSystemCriticalException {
        Path path = buildPathFromResources(PLUGIN_CONFIG_FILE);

        DefaultPluginConfig defaultPluginConfig = new DefaultPluginConfig(path);

        assertNotNull(defaultPluginConfig);
    }

    /**
     * This test was created because after downloading plugin with the same name and version,
     * it was multiplied in Set. Problem was caused by inappropriate implementation of hashCode
     * method. After fix, there is no possible to add twice plugin's config that has:
     * name, version and enabled property
     * */
    @Test
    @DisplayName("Should not add twice the same object to Map<String, Set<ConfigDescription>> configDescriptions")
    void uniquePluginConfigAddingOnFlyTest() {
        PluginsConfigFile pluginsConfigFile = new PluginsConfigFile();

        final String INTERFACE_NAME = "io.easeci.extension.bootstrap.OnStartup";

//        Two same objects with different UUIDs and 'enabled' fiels
        ConfigDescription configDescription_a = new ConfigDescription(UUID.randomUUID(), "welcome-logo", "0.0.1", true);
        ConfigDescription configDescription_b = new ConfigDescription(UUID.randomUUID(), "welcome-logo", "0.0.1", true);
        ConfigDescription configDescription_c = new ConfigDescription(UUID.randomUUID(), "welcome-logo", "0.0.1", false);

        boolean isAdded_a = pluginsConfigFile.put(INTERFACE_NAME, configDescription_a);
        assertTrue(isAdded_a);
        assertEquals(1, new ArrayList<>(pluginsConfigFile.getConfigDescriptions().get(INTERFACE_NAME)).size());

        boolean isAdded_b = pluginsConfigFile.put(INTERFACE_NAME, configDescription_b);
        assertFalse(isAdded_b);
        assertEquals(1, new ArrayList<>(pluginsConfigFile.getConfigDescriptions().get(INTERFACE_NAME)).size());

        boolean isAdded_c = pluginsConfigFile.put(INTERFACE_NAME, configDescription_c);
        assertFalse(isAdded_c);
        assertEquals(1, new ArrayList<>(pluginsConfigFile.getConfigDescriptions().get(INTERFACE_NAME)).size());
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