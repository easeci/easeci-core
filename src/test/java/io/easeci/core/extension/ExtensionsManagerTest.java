package io.easeci.core.extension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionsManagerTest {

    @Test
    @DisplayName("Should correctly shutdown plugin other than Standalone.class")
    void shutdownExtensionTest() {
//        TODO
    }

    @Test
    @DisplayName("Should not shutdown plugin that not exists in container and is other than Standalone.class")
    void shutdownExtensionFailureTest() {
//        TODO

    }

    @Test
    @DisplayName("Should correctly shutdown plugin that is Standalone extension")
    void shutdownExtensionStandaloneTest() {
//        TODO

    }

    @Test
    @DisplayName("Should not shutdown plugin that is Standalone extension but it is no such plugin in container")
    void shutdownExtensionStandaloneFailureTest() {
//        TODO

    }

    @Test
    @DisplayName("Should correctly startup plugin that was not runnning before")
    void startupExtensionTest() {
//        TODO
    }
}