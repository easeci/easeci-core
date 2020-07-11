package io.easeci.core.extension;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class ExtensionSystemTest {

    @BeforeAll
    static void setup() {
        ExtensionsManager extensionsManager = Mockito.mock(ExtensionsManager.class);
        PluginContainer pluginContainer = Mockito.mock(PluginContainer.class);

        Mockito.when(extensionsManager.getPluginContainer()).thenReturn(pluginContainer);
        Mockito.when(pluginContainer.getGathered("some.interface", String.class)).thenReturn(provideInstancesList());
    }

    static List<String> provideInstancesList() {
        List<String> stringInstances = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            stringInstances.add("Instance no. " + i);
        }
        return stringInstances;
    }

    @Test
    @DisplayName("Should correctly instantiate ExtensionSystem with expected thread pool's size")
    void extensionSystemInitTest() throws PluginSystemCriticalException {
        ExtensionSystem instance = ExtensionSystem.getInstance();

        assertEquals(1, instance.getThreadPoolMaxSize());
    }

    @Test
    @DisplayName("Should correctly set boolean flag 'started' when ExtensionSystem is running")
    void extensionSystemStartTest() throws PluginSystemCriticalException {
        ExtensionSystem extensionSystem = ExtensionSystem.getInstance();
        extensionSystem.start();

        assertTrue(extensionSystem.isStarted());
    }

    @Disabled
    @Test
    @DisplayName("Should not allow create threads more than it is specified in threadPoolMaxSize variable")
    void extensionSystemOverflowedTest() throws PluginSystemCriticalException {
        ExtensionSystem extensionSystem = ExtensionSystem.getInstance();
        extensionSystem.start();
        List<String> all = extensionSystem.getAll("some.interface", String.class);

//        Mock not works, fix it TODO
    }

    @Disabled
    @Test
    @DisplayName("Should throw RuntimeException when trying to get all plugins without starting mechanism")
    void extensionSystemNoStartedTest() {
        assertThrows(RuntimeException.class, () -> ExtensionSystem.getInstance().getAll("some.interface", String.class));
    }
}