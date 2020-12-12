package io.easeci.core.engine.easefile.loader;

import io.easeci.BaseWorkspaceContextTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static commons.WorkspaceTestUtils.buildPathFromResources;
import static org.junit.jupiter.api.Assertions.*;

class WorkspaceLoaderTest extends BaseWorkspaceContextTest {

    static final int EASEFILE_LENGTH = 790;

    @Test
    @DisplayName("Should correctly load Easefile to string without any exception")
    void loadSuccessTest() throws IOException {
        Path path = buildPathFromResources("workspace/Easefile");

        WorkspaceLoader workspaceLoader = (WorkspaceLoader) WorkspaceLoader.of(path.toString());
        String easefileContent = workspaceLoader.testProvide();

        assertEquals(EASEFILE_LENGTH, easefileContent.length());
    }

    @Test
    @DisplayName("Should throw I/O exception while loading file from workspace (file not exists)")
    void loadFailureTest() {
        Path path = Paths.get("/tmp/not-existing");

        WorkspaceLoader workspaceLoader = (WorkspaceLoader) WorkspaceLoader.of(path.toString());

        assertThrows(IOException.class, workspaceLoader::testProvide);
    }

    @Test
    @DisplayName("Should throw exception because we can not read file out of workspace scope")
    void loadAccessDeniedTest() {
        Path path = Paths.get("/tmp/some-file");

        WorkspaceLoader workspaceLoader = (WorkspaceLoader) WorkspaceLoader.of(path.toString());

        assertThrows(IllegalAccessException.class, workspaceLoader::provide);
    }
}