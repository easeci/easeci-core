package io.easeci.core.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.easeci.core.extension.utils.PluginContainerUtils.fromBasic;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginContainerTest {
    private PluginContainer pluginContainer;
    private PluginStrategy pluginStrategy = Mockito.mock(PluginStrategy.class);

    @BeforeEach
    void setup() {
        this.pluginContainer = new DefaultPluginContainer(pluginStrategy);
    }

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

        assertEquals(1, pluginContainer.size());
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

        assertEquals(1, pluginContainer.size());
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

        assertEquals(2, pluginContainer.size());
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

        assertEquals(1, pluginContainer.size());
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

        assertEquals(1, pluginContainer.size());
        assertEquals(2, pluginContainer.implementationSize(INTERFACE_NAME));
    }

    @Test
    @DisplayName("Should return 0 (zero) when container is empty")
    void defaultPluginContainerSizeTest() {
        assertEquals(0, pluginContainer.size());
    }
}