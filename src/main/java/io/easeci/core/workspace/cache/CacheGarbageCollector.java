package io.easeci.core.workspace.cache;

import java.nio.file.Path;

public interface CacheGarbageCollector {

    // Path must be here ../workspace/.cache/
    void delayedCleanup(Path path);
}
