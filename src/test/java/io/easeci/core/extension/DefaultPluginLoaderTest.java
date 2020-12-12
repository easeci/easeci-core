package io.easeci.core.extension;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.extension.ExtensionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static io.easeci.core.extension.utils.PluginContainerUtils.createCorrectFakePlugin;
import static io.easeci.core.extension.utils.PluginContainerUtils.createFakePlugin;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class DefaultPluginLoaderTest extends BaseWorkspaceContextTest {
    private final static String PLUGIN_CONFIG_FILE_DISABLED = "workspace/plugins-config-test-all-disabled.json",
                                   PLUGIN_CONFIG_FILE_VALID = "workspace/plugins-config-test.json";
    private static Path pluginConfigJsonDisabledPath = buildPathFromResources(PLUGIN_CONFIG_FILE_DISABLED),
                        pluginConfigJsonValidPath = buildPathFromResources(PLUGIN_CONFIG_FILE_VALID);

    @Test
    @DisplayName("Should not load plugin that is disabled in plugins-config.json")
    void loadPluginsNotLoadDisabledPluginsTest() throws PluginSystemCriticalException {
//        prepare required objects
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigJsonDisabledPath);
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

    @Test
    @DisplayName("Should not load the same plugin twice if just exists in container (is just loaded)")
    void loadPluginsIdempotencyTest() throws PluginSystemCriticalException {
//        prepare required objects
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigJsonValidPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);
        JarJoiner jarJoinerMock = Mockito.mock(JarJoiner.class);

//        prepare plugins, load plugin definition from plugins.yml (test/resources)
        Set<Plugin> resolvedPlugins = createCorrectFakePlugin();

//        mock JarJoiner method's behavior
        Mockito.when(jarJoinerMock.addToClasspath(any())).thenReturn(new ArrayList<>(resolvedPlugins).get(0));
        Mockito.when(jarJoinerMock.read(any())).thenReturn(ExtensionManifest.of("", ""));

//        create SUT
        PluginLoader pluginLoader = new DefaultPluginLoader(pluginContainer, jarJoinerMock);

//       ->  execute plugin loading
        Set<Plugin> rejectedPlugins = pluginLoader.loadPlugins(resolvedPlugins, pluginStrategy);

        assertAll(() -> assertEquals(2, resolvedPlugins.size()),         // total resolved from file (mocked from createCorrectFakePlugin())
                () -> assertEquals(1, rejectedPlugins.size()),           // total rejected, multiplied, doubled
                () -> assertEquals(1, pluginContainer.keySize()),        // total container key size
                () -> assertEquals(1, pluginContainer.instanceSize()));  // total instances created in container
    }
}