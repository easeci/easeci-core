package io.easeci.core.extension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginContainerStateTest {

    @Test
    @DisplayName("Should return correct JSON structure in order to display info for user")
    void dumpTest() {
        PluginContainerState pluginContainerState = new PluginContainerState();

        String dump = pluginContainerState.dump();
    }
}