package io.easeci.core.workspace.easefiles;

import io.easeci.BaseWorkspaceContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static io.easeci.core.workspace.LocationUtils.getEasefilesStorageLocation;
import static org.junit.jupiter.api.Assertions.*;

class DefaultEasefileManagerTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly create plugin files storage (Easefile storage) in workspace after object was created")
    void instantiateAndCreateDirectory() {
        EasefileManager easefileManager = DefaultEasefileManager.getInstance();
        String easefilesStorageLocation = getEasefilesStorageLocation();

        assertNotNull(easefileManager);
        assertTrue(Files.exists(Paths.get(easefilesStorageLocation)));
    }

}