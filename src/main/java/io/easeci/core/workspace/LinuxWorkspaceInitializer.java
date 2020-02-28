package io.easeci.core.workspace;

import io.easeci.utils.io.YamlUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Standard, default implementation of WorkspaceInitializer interface.
 * Defines .run.yml file format, and EaseCI core bootstrapping process.
 * @author Karol Meksuła
 * 2020-01-26
 * */

@Slf4j
public class LinuxWorkspaceInitializer extends AbstractWorkspaceInitializer {
    private static LinuxWorkspaceInitializer linuxWorkspaceInitializer;

    private final List<String> FILE_NAMES = List.of("general.yml");

    private LinuxWorkspaceInitializer() {}

    public static LinuxWorkspaceInitializer getInstance() {
        if (isNull(linuxWorkspaceInitializer)) {
            linuxWorkspaceInitializer = new LinuxWorkspaceInitializer();
        }
        return linuxWorkspaceInitializer;
    }

    /**
     * This implementation is able to copy files from resources
     * to workspace with nested-parent directories. If you put
     * file in resources/workspace/nested/directory/file.txt,
     * so add simply new String "nested/directory/file.txt" to
     * List<String> FILE_NAMES in class-level field.
     * Watch out for slashes!
     * */
    @SneakyThrows
    @Override
    Path copyConfig(Path mainWorkspacePath) {
        return copyFiles(mainWorkspacePath).getValue0();
    }

    @SneakyThrows
    private Pair<Path, Set<String>> copyFiles(Path mainWorkspacePath) {
        final String RESOURCES_PATH = "workspace/";
        final String TARGET_PATH = mainWorkspacePath.toString().concat("/");
        Set<String> filenames = new HashSet<>(Set.of());

        for (String filename : FILE_NAMES) {
            if (filename.contains("/")) {
                String parentDirectory = removePath(filename, 1);
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
            filenames.add(to.toString());
            log.info("Copied correctly file: {} to {}", from, to);
        }
        return Pair.with(mainWorkspacePath, filenames);
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
                try {
                    super.validatePath(workspacePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                createRunYml(bootstrapFilePath(), workspacePath);
                copyConfig(workspacePath);
            }, () -> {
                Path workspacePath = Path.of(currentDir().toString().concat("/workspace"));
                log.error("Optional<Path> is empty! You did not provide path in argument of .jar runtime.\n" +
                        "Now EaseCI will create workspace in current directory: {}", workspacePath.toString());
                try {
                    Files.createDirectory(workspacePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                createRunYml(Paths.get(currentDir().toString()
                        .concat("/")
                        .concat(BOOTSTRAP_FILENAME)), workspacePath);
                copyConfig(workspacePath);
            });
            return init(path);
        });
    }

    private Path bootstrapFilePath() {
        return Path.of(System.getProperty("user.dir")
                .concat("/")
                .concat(BOOTSTRAP_FILENAME));
    }

    private Path currentDir() {
        return Path.of(System.getProperty("user.dir"));
    }

    private Path removePath(Path path, int elements) {
        String[] parts = path.toString().split("/");
        return Paths.get(String.join("/", Arrays.copyOf(parts, parts.length - elements)));
    }

    private String removePath(String path, int elements) {
        String[] parts = path.split("/");
        return String.join("/", Arrays.copyOf(parts, parts.length - elements));
    }

    private Optional<Path> findRunYml() {
        Path filePath = bootstrapFilePath();
        if (Files.exists(filePath)) {
            return Optional.of(filePath);
        }
        return Optional.empty();
    }

    @Override
    public Triplet<Boolean, Path, Set<String>> scan(Path workspacePath) throws IllegalStateException {
        log.info("==> Scanning for filesystem integration in workspace of EaseCI");
        final Path RUN_FILE_PATH = locateBootstrapFile();
        String workspaceLocation = workspaceLocation();

        Set<String> filesNotFound = FILE_NAMES.stream()
                .map(filename -> workspaceLocation.concat("/").concat(filename))
                .peek(filenamePath -> log.info("====> Looking for a file: {}", filenamePath))
                .filter(filenamePath -> !Files.exists(Paths.get(filenamePath)))
                .peek(filenamePath -> log.error("====> Could't find file with path {}", filenamePath))
                .collect(Collectors.toSet());

        Boolean result = filesNotFound.isEmpty();
        log.info("==>Scanning finished. Is workspace valid - {}", result.toString());
        return Triplet.with(result, RUN_FILE_PATH, filesNotFound);
    }

    @Override
    public Pair<Boolean, Set<File>> fix(Path workspacePath) {
        Path workspacePathFromYml = Paths.get(workspaceLocation());
        log.info("==> Started to fixing integration of files in EaseCI workspace detected here: {}", workspacePathFromYml);

        if (!Files.isDirectory(workspacePathFromYml)) {
            log.info("===> Workspace directory [{}] not exists.\n" +
                    "Whole workspace initialize process starting...", workspacePathFromYml);
            try {
                Files.deleteIfExists(locateBootstrapFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.init(Optional.of(workspacePathFromYml));
            Triplet<Boolean, Path, Set<String>> scanResult = this.scan(workspacePathFromYml);
            if (scanResult.getValue0()) {
                log.info("====> Successfully recreated workspace content and placed whole files here: {}", scanResult.getValue1().toString());
            } else {
                log.error("! ===> Something went wrong and workspace recreation could not end with success");
            }
            return Pair.with(scanResult.getValue0(), scanResult.getValue2()
                    .stream()
                    .map(File::new)
                    .collect(Collectors.toSet()));
        }
        Pair<Path, Set<String>> copyResult = copyFiles(workspacePathFromYml);
        return Pair.with(nonNull(copyResult.getValue0()),
                copyResult.getValue1().stream()
                        .map(File::new)
                        .collect(Collectors.toSet()));
    }

    private Path locateBootstrapFile() {
        return Paths.get(currentDir().toString().concat("/").concat(BOOTSTRAP_FILENAME));
    }

    private String workspaceLocation() {
        Path runfile = locateBootstrapFile();
        return (String) YamlUtils.ymlGet(runfile, "easeci.workspace.path").getValue();
    }
}
