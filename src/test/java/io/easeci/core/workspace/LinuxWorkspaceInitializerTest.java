package io.easeci.core.workspace;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.commons.YamlUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.easeci.core.workspace.LinuxWorkspaceInitializer.BOOTSTRAP_FILENAME;
import static org.junit.jupiter.api.Assertions.*;

class LinuxWorkspaceInitializerTest extends BaseWorkspaceContextTest {
    private LinuxWorkspaceInitializer workspaceInitializer;

    private final Path TEST_WORKSPACE_PATH = Paths.get("/tmp/easeci_test");

    @BeforeEach
    void setupEach() {
        this.workspaceInitializer = LinuxWorkspaceInitializer.getInstance();
    }

    @Test
    @DisplayName("Should correctly initialize workspace in indicated directory")
    void initTest() {
        Optional<Path> optionalPath = Optional.of(TEST_WORKSPACE_PATH);
        workspaceInitializer.init(optionalPath);

        Path pwd = Path.of(System.getProperty("user.dir"));
        Path runYmlPath = Paths.get(pwd.toString().concat("/").concat(BOOTSTRAP_FILENAME));

        assertAll(() -> assertTrue(Files.exists(runYmlPath)),
                () -> assertTrue(Files.exists(Paths.get(String.valueOf(YamlUtils.ymlGet(runYmlPath, "easeci.workspace.path").getValue())))));
    }

    @Test
    @DisplayName("Should correctly initialize workspace in current direcory if Optional<Path> is empty")
    void initEmptyTest() {
        Optional<Path> optionalPath = Optional.empty();
        workspaceInitializer.init(optionalPath);

        Path pwd = Path.of(System.getProperty("user.dir"));
        Path runYmlPath = Paths.get(pwd.toString().concat("/").concat(BOOTSTRAP_FILENAME));

        assertAll(() -> assertTrue(Files.exists(runYmlPath)),
                () -> assertTrue(Files.exists(Paths.get(String.valueOf(YamlUtils.ymlGet(runYmlPath, "easeci.workspace.path").getValue())))));
    }
}