package io.easeci.core.workspace;

import io.easeci.utils.io.YamlUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Objects.isNull;

/**
 * Standard, default implementation of WorkspaceInitializer interface.
 * Defines .run.yml file format, and EaseCI core bootstrapping process.
 * @author Karol Meksuła
 * 2020-01-26
 * */

@Slf4j
public class StandardWorkspaceInitializer extends AbstractWorkspaceInitializer {
    public final static String BOOTSTRAP_FILENAME = ".run.yml";

    /**
     * This implementation is able to copy files from resources
     * to workspace with nested-parent directories. If you put
     * file in resources/workspace/nested/directory/file.txt,
     * so add simply new String "nested/directory/file.txt" to
     * List<String> FILE_NAMES in code bellow.
     * Watch out for slashes!
     * */
    @SneakyThrows
    @Override
    Path copyConfig(Path mainWorkspacePath) {
        final String RESOURCES_PATH = "workspace/";
        final List<String> FILE_NAMES = List.of("general.yml");
        final String TARGET_PATH = mainWorkspacePath.toString().concat("/");

        for (String filename : FILE_NAMES) {
            if (filename.contains("/")) {
                String[] parts = filename.split("/");
                parts = Arrays.copyOf(parts, parts.length - 1);
                String parentDirectory = String.join("/", parts);
                Files.createDirectories(Paths.get(TARGET_PATH.concat(parentDirectory)));
            }
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCES_PATH.concat(filename));
            String from = RESOURCES_PATH.concat(filename);
            Path to = Paths.get(TARGET_PATH.concat(filename));
            try {
                if (isNull(inputStream)) {
                    log.error("Cannot load file {} so InputStream is null!", from);
                    break;
                }
                Files.createFile(to);
                FileUtils.copyInputStreamToFile(inputStream, to.toFile());
            } catch (IOException e) {
                log.error("Could not copy file: {}", from);
                break;
            }
            log.info("Copied correctly file: {} to {}", from, to);
        }
        return mainWorkspacePath;
    }

    @Override
    Path createRunYml(Path path, Path workspaceLocation) {
        return YamlUtils.ymlCreate(path, new LinkedHashMap<>() {{
            put("easeci", new LinkedHashMap<>() {{
                put("workspace", new LinkedHashMap<>() {{
                    put("path", workspaceLocation.toAbsolutePath().toString());
                }});
            }});
        }});
    }

    @Override
    public Path init(Optional<Path> path) {
        Optional<Path> runFile = findRunYml();
        runFile.ifPresent(p -> log.info(".run.yml file detected in: " + p.toString()));
        return runFile.orElseGet(() -> {
            log.info("Cannot find .run.yml file, so such file will created now");
            path.ifPresentOrElse(workspacePath -> {
                createRunYml(currentDir(), workspacePath);
                copyConfig(workspacePath);
            }, () -> log.info("Something went wrong and Optional<Path> is empty!"));
            return init(path);
        });
    }

    private Path currentDir() {
        return Path.of(System.getProperty("user.dir")
                .concat("/")
                .concat(BOOTSTRAP_FILENAME));
    }

    private Optional<Path> findRunYml() {
        Path filePath = currentDir();
        if (Files.exists(filePath)) {
            return Optional.of(filePath);
        }
        return Optional.empty();
    }

    @Override
    public Boolean validate(Path dataLocation) {
        return null;
    }
}
