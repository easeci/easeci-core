package io.easeci.core.workspace;

import io.easeci.utils.io.YamlUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Standard, default implementation of WorkspaceInitializer interface.
 * Defines .run.yml file format, and EaseCI core bootstrapping process.
 * @author Karol Meksuła
 * 2020-01-26
 * */

@Slf4j
public class StandardWorkspaceInitializer extends AbstractWorkspaceInitializer {
    public final static String BOOTSTRAP_FILENAME = ".run.yml";

    @Override
    Path copyConfig(Path path) {
        return null;
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
            createRunYml(currentDir(), path.orElseThrow());
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
