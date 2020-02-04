package io.easeci.core.workspace;

import io.easeci.utils.io.YamlUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.easeci.core.workspace.LinuxWorkspaceInitializer.BOOTSTRAP_FILENAME;
import static org.junit.jupiter.api.Assertions.*;

class LinuxWorkspaceInitializerTest {
    private LinuxWorkspaceInitializer workspaceInitializer;

    private final Path TEST_WORKSPACE_PATH = Paths.get("/tmp/easeci_test");

    @BeforeEach
    void setup() {
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

    void deleteRunYaml() throws IOException {
        Path pwd = Path.of(System.getProperty("user.dir"));
        Path runYmlPath = Paths.get(pwd.toString().concat("/").concat(BOOTSTRAP_FILENAME));
        Files.deleteIfExists(runYmlPath);
    }

    @AfterEach
    void cleanup() throws IOException {
        FileUtils.deleteDirectory(TEST_WORKSPACE_PATH.toFile());
        deleteRunYaml();
    }
}