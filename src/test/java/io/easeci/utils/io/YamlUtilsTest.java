package io.easeci.utils.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlUtilsTest {
    private final static String FILE_PATH = "/tmp/example.file";
    private final static String UNRECOGNIZED_PATH = "/tmp/not/existing/path";

    private static List<YamlUtils.YmlField<?>> provideYmlFields() {
        return List.of(
                new YamlUtils.YmlField("main.paths.temp", String.valueOf("/tmp/ease")),
                new YamlUtils.YmlField("output.autopublishing", Boolean.valueOf("false")),
                new YamlUtils.YmlField("output.queue.max-size", Integer.parseInt("100")),
                new YamlUtils.YmlField("main.paths.home", String.valueOf("/usr/local/ease"))
        );
    }

    @Test
    @DisplayName("Should load yaml to map with success")
    void loadYamlSuccessTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);

        Map<?, ?> yaml =YamlUtils.ymlLoad(path);
        assertEquals(2, yaml.size());
    }

    @Test
    @DisplayName("Should return Exception when trying to load invalid yaml file")
    void loadYamlFailureTest() {
        final String FILE_CONTENT = Utils.ymlInvalidContent();
        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);

        assertThrows(Exception.class, () -> YamlUtils.ymlLoad(path));
    }

    @Test
    @DisplayName("Should return correctly values defined in yaml at correctly specified key (refs)")
    void yamlGetSuccessTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);

        provideYmlFields()
                .forEach(testYmlField -> {
                    YamlUtils.YmlField<?> field = YamlUtils.ymlGet(path, testYmlField.getReferenceKey());
                    assertAll(() -> assertEquals(testYmlField.getReferenceKey(), testYmlField.getReferenceKey()),
                            () -> assertEquals(testYmlField.getValue(), field.getValue()));
                });
    }

    @Test
    @DisplayName("Should return LinkedHashMap when trying to load not primitive type of variable (nested structure)")
    void yamlGetMapTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);

        assertEquals(LinkedHashMap.class, YamlUtils.ymlGet(path, "main.paths").getValue().getClass());
    }

    @Test
    @DisplayName("Should throw exception when trying to load value from key that not exists")
    void yamlGetFailureTest() {
        final String FILE_CONTENT = Utils.ymlContent();
        Path path = FileUtils.fileSave(FILE_PATH, FILE_CONTENT, false);

        assertThrows(YamlUtils.YamlException.class, () -> YamlUtils.ymlGet(path, "not.existing.path"));
    }

    @AfterEach
    void cleanup() {
        try {
            Files.deleteIfExists(Path.of(FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}