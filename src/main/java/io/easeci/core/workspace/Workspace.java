package io.easeci.core.workspace;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Functional interface that defines way, how workspace should be
 * initialized on application startup.
 * @author Karol Meksuła
 * 2020-01-26
 * */
public interface Workspace {

    /**
     * Initialize workspace. If EaseCI workspace does not exists
     * then create this one in specified location in Path in argument.
     * @param path is Optional object that defines where workspace should
     *             be created. If workspace exists right now, path will be
     *             ignored.
     * @return Path where workspace was just initialized.
     * */
    Path init(Optional<Path> path);
}
