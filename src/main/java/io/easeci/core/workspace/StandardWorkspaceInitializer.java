package io.easeci.core.workspace;

import java.nio.file.Path;
import java.util.Optional;

public class StandardWorkspaceInitializer extends AbstractWorkspaceInitializer {

    @Override
    Path copyConfig(Path path) {
        return null;
    }

    @Override
    Path createRunYml(Path path) {
        return null;
    }

    @Override
    public Path init(Optional<Path> path) {
        path.ifPresent(path1 -> System.out.println(path1));
        return Path.of("/");
    }
}
