package io.easeci.core.extension;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.extension.ExtensionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static io.easeci.core.extension.utils.PluginContainerUtils.fromBasic;
import static io.easeci.core.extension.utils.PluginContainerUtils.fromBasicWithPluginName;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginContainerTest extends BaseWorkspaceContextTest {
    private final static String PLUGIN_CONFIG_FILE = "workspace/plugins-config-test.json";
    private PluginStrategy pluginStrategy = Mockito.mock(PluginStrategy.class);
    private PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

    @Test
    @DisplayName("Should correctly create container object")
    void defaultPluginContainerCreationTest() {
        assertNotNull(pluginContainer);
    }

    @Test
    @DisplayName("Should add new interface and new implementation to container")
    void defaultPluginContainerAddTest() {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION = "This is implementation";

        Instance instance = fromBasic(INTERFACE_NAME, IMPLEMENTATION);
        pluginContainer.add(instance);

        assertEquals(1, pluginContainer.keySize());
    }

    /* Adding a new instance should not duplicate the key in the container map.
    * New instance must be added to list assigned to key.*/
    @Test
    @DisplayName("Should add new implementation to container - one implementation just exists, interface is added as a key")
    void defaultPluginContainerAddNextTest() {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B);

        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        assertEquals(1, pluginContainer.keySize());
    }

    @Test
    @DisplayName("Should add new interface to container")
    void defaultPluginContainerAddNextInterfaceTest() {
        final String INTERFACE_NAME_A = "java.lang.String";
        final String INTERFACE_NAME_B = "groovy.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Instance instanceA = fromBasic(INTERFACE_NAME_A, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME_B, IMPLEMENTATION_B);

        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        assertEquals(2, pluginContainer.keySize());
        assertEquals(1, pluginContainer.implementationSize(INTERFACE_NAME_A));
        assertEquals(1, pluginContainer.implementationSize(INTERFACE_NAME_B));
    }

    @Test
    @DisplayName("Should not add two identical object associated with one key of container's map")
    void defaultPluginContainerAddNextTheSamePluginFailTest() {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION = "This is implementation";

        Instance instance = fromBasic(INTERFACE_NAME, IMPLEMENTATION);
        Instance instanceDuplicate = fromBasic(INTERFACE_NAME, IMPLEMENTATION);

        pluginContainer.add(instance);
        pluginContainer.add(instanceDuplicate);

        assertEquals(1, pluginContainer.keySize());
        assertEquals(1, pluginContainer.implementationSize(INTERFACE_NAME));
    }

    @Test
    @DisplayName("Should add correctly two object associated with one key of container's map")
    void defaultPluginContainerAddNextTheSamePluginSuccessTest() {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B, "another-plugin-implements-same-interface", "0.0.1");

        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        assertEquals(1, pluginContainer.keySize());
        assertEquals(2, pluginContainer.implementationSize(INTERFACE_NAME));
    }

    @Test
    @DisplayName("Should return 0 (zero) when container is empty")
    void defaultPluginContainerSizeTest() {
        assertEquals(0, pluginContainer.keySize());
    }

    @Test
    @DisplayName("Should correctly get concrete implementation with getSpecific(..) method where there is only one object")
    void defaultPluginContainerGetSpecific() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        pluginContainer.add(instanceA);

        String specific = pluginContainer.getSpecific(INTERFACE_NAME, String.class);

        assertEquals(1, pluginContainer.keySize());
        assertEquals(IMPLEMENTATION_A, specific);
    }

    @Test
    @DisplayName("Should correctly get concrete implementation with getSpecific(..) when there is more than one object")
    void defaultPluginContainerMultiObjectsGetSpecific() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B);
        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        String specific = pluginContainer.getSpecific(INTERFACE_NAME, String.class);

        assertEquals(1, pluginContainer.keySize());
        assertEquals(IMPLEMENTATION_A, specific);
    }

    @Test
    @DisplayName("Should return empty list when cannot find interface name in container")
    void defaultPluginContainerInterfaceNotRecognizedGetSpecific() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        String specific = pluginContainer.getSpecific(INTERFACE_NAME, String.class);

        assertNull(specific);
    }

    @Test
    @DisplayName("Should return null when trying to cast instance to not matching type")
    void defaultPluginContainerCastExceptionGetSpecific() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        pluginContainer.add(instanceA);

        Double specific = pluginContainer.getSpecific(INTERFACE_NAME, Double.class);

        assertNull(specific);
    }

    @Test
    @DisplayName("Should correctly get all instances of Standalone interface")
    void defaultPluginContainerGetGathered() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B, "0.0.2");
        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        List<String> gathered = pluginContainer.getGathered(INTERFACE_NAME, String.class);

        assertEquals(2, gathered.size());
    }

    @Test
    @DisplayName("Should return empty list when there is any implementation of Standalone interface in container")
    void defaultPluginContainerEmptyListGetGathered() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        List<String> gathered = pluginContainer.getGathered(INTERFACE_NAME, String.class);

        assertTrue(gathered.isEmpty());
    }

    @Test
    @DisplayName("Should not return element on list when cast exception occurred (null value should be dropped from Stream)")
    void defaultPluginContainerCastExceptionGetGathered() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B, "0.0.2");
        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        List<Double> gathered = pluginContainer.getGathered(INTERFACE_NAME, Double.class);

        assertTrue(gathered.isEmpty());
    }

    @Test
    @DisplayName("Should correctly find Instance.class identified with UUID passed in method argument")
    void defaultPluginContainerFindByUuid() throws PluginSystemCriticalException {
        final UUID PLUGIN_UUID = UUID.fromString("4593a486-776b-11ea-bc55-0242ac130003");   // this is value from plugins-config-test.json
        final ExtensionType EXTENSION_TYPE = ExtensionType.EXTENSION_PLUGIN;               // same ^

        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";
        final String IMPLEMENTATION_C = "This is implementation C";
        final String IMPLEMENTATION_D = "This is implementation D";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B, "0.0.2");
        Instance instanceC = fromBasic(INTERFACE_NAME, IMPLEMENTATION_C, "0.0.1");
        Instance instanceD = fromBasicWithPluginName(INTERFACE_NAME, IMPLEMENTATION_D, "welcome-logo", "0.0.1");
        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);
        pluginContainer.add(instanceC);
        pluginContainer.add(instanceD);

        Optional<Instance> instance = pluginContainer.findByUuid(EXTENSION_TYPE, PLUGIN_UUID);

        assertAll(() -> assertTrue(instance.isPresent()),
                () -> instance.ifPresent(inst -> {
                    assertEquals(IMPLEMENTATION_D, inst.getInstance());
                    assertEquals("welcome-logo", inst.getPlugin().getName());
                    assertEquals("0.0.1", inst.getPlugin().getVersion());
                }));
    }

    @Test
    @DisplayName("Should throw exception when cannot find configuration of plugin")
    void defaultPluginContainerFindByUuidInvalidTest() throws PluginSystemCriticalException {
        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        assertThrows(PluginSystemIntegrityViolated.class, () -> pluginContainer.findByUuid(ExtensionType.EXTENSION_PLUGIN, UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should not find Instance.class because one not exists in container and not specified this UUID in plugins-config.json file")
    void defaultPluginContainerFindByUuidFail() {
        final UUID PLUGIN_UUID = UUID.fromString("4593a486-776b-11ea-bc55-0242ac130006");   // this is value from plugins-config-test.json
        final ExtensionType EXTENSION_TYPE = ExtensionType.EXTENSION_PLUGIN;               // same ^

        Optional<Instance> instance = pluginContainer.findByUuid(EXTENSION_TYPE, PLUGIN_UUID);

        assertFalse(instance.isPresent());
    }

    @Test
    @DisplayName("Should correctly remove instance from Container and return 'true' boolean value")
    void defaultPluginContainerRemoveByPluginNameAndVersionSuccessTest() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        final String IMPLEMENTATION_A = "This is implementation A";
        final String IMPLEMENTATION_B = "This is implementation B";

        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        Instance instanceB = fromBasic(INTERFACE_NAME, IMPLEMENTATION_B, "0.0.2");
        pluginContainer.add(instanceA);
        pluginContainer.add(instanceB);

        assertAll(() -> assertEquals(2, pluginContainer.implementationSize(INTERFACE_NAME)),
                  () -> assertEquals(2, pluginContainer.instanceSize()),
                  () -> assertEquals(1, pluginContainer.keySize()));

        Plugin pluginA = instanceA.getPlugin();
        Plugin pluginB = instanceB.getPlugin();

        boolean isPluginBRemoved = pluginContainer.remove(pluginB.getName(), pluginB.getVersion());

        assertAll(() -> assertTrue(isPluginBRemoved),
                () -> assertEquals(1, pluginContainer.implementationSize(INTERFACE_NAME)),
                () -> assertEquals(1, pluginContainer.instanceSize()),
                () -> assertEquals(1, pluginContainer.keySize()));
    }

    @Test
    @DisplayName("Should not remove instance from Container because one not exists and return 'false' boolean value")
    void defaultPluginContainerRemoveByPluginNameAndVersionNotRemovedTest() throws PluginSystemCriticalException {
        final String INTERFACE_NAME = "java.lang.String";
        Path pluginConfigPath =  buildPathFromResources(PLUGIN_CONFIG_FILE);
        PluginStrategy pluginStrategy = new DefaultPluginConfig(pluginConfigPath);
        PluginContainer pluginContainer = new DefaultPluginContainer(pluginStrategy);

        final String IMPLEMENTATION_A = "This is implementation A";
        Instance instanceA = fromBasic(INTERFACE_NAME, IMPLEMENTATION_A);
        pluginContainer.add(instanceA);

        boolean isPluginRemoved = pluginContainer.remove("not-exists", "0.0.1");

        assertAll(() -> assertFalse(isPluginRemoved),
                () -> assertEquals(1, pluginContainer.implementationSize(INTERFACE_NAME)),
                () -> assertEquals(1, pluginContainer.instanceSize()),
                () -> assertEquals(1, pluginContainer.keySize()));
    }
}