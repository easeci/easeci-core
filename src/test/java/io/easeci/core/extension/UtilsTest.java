package io.easeci.core.extension;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UtilsTest {

    @Disabled // works only when you provide correct URL to appropriate jar archive
    @Test
    @DisplayName("Should correctly retrieve ExtensionManifest.java object from jar archive")
    void extractManifestTest () throws IOException {
        Path localJarPath = Paths.get("/home/karol/dev/java/easeci-core-java/plugins/welcome-logo-0.0.1.jar");

        ExtensionManifest extensionManifest = Utils.extractManifest(localJarPath);

        assertNotNull(extensionManifest.getEntryClassProperty());
        assertNotNull(extensionManifest.getImplementsProperty());
    }
}
