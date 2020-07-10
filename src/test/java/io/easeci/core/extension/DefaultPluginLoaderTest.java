package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static io.easeci.core.extension.utils.PluginContainerUtils.createFakePlugin;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class DefaultPluginLoaderTest {
    private final static String PLUGIN_CONFIG_FILE = "workspace/plugins-config-test-all-disabled.json";
    private static Path pluginConfigJsonPath;

    @BeforeEach
    void setup() {
        pluginConfigJsonPath = buildPathFromResources(PLUGIN_CONFIG_FILE);
    }

    @Test
    @DisplayName("Should not load plugin that is disabled in plugins-config.json")
    void loadPluginsNotLoadDisabledPluginsTest() {
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigJsonPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);
        JarJoiner jarJoinerMock = Mockito.mock(JarJoiner.class);

//        prepare plugins
        Plugin fakePlugin = createFakePlugin();
        Set<Plugin> pluginSet = Set.of(fakePlugin);

//        mock JarJoiner method's behavior
        Mockito.when(jarJoinerMock.addToClasspath(any())).thenReturn(fakePlugin);
        Mockito.when(jarJoinerMock.read(any())).thenReturn(ExtensionManifest.of("", ""));

//        create SUT
        PluginLoader pluginLoader = new DefaultPluginLoader(pluginContainer, jarJoinerMock);

//       ->  execute plugin loading
        pluginLoader.loadPlugins(pluginSet, pluginStrategy);

//        UUID from plugins-config-test-all-disabled.json
        UUID pluginUuid = UUID.fromString("919f57ca-776c-11ea-bc55-0242ac130003");
//        Extension type defined in plugins-config-test-all-disabled.json
        ExtensionType extensionType = ExtensionType.EXTENSION_PLUGIN;

//        after loading plugins, container should have Instance.class of this plugin
//        but object should not working and should be null
        Optional<Instance> instanceOptional = pluginContainer.findByUuid(extensionType, pluginUuid);

        assertAll(() -> assertTrue(instanceOptional.isPresent()),
                () -> instanceOptional.ifPresent(instance -> {
                    assertNull(instance.getInstance());
                    assertEquals(instance.getPlugin().getName(), fakePlugin.getName());
                    assertEquals(instance.getPlugin().getVersion(), fakePlugin.getVersion());
                }));
    }
}