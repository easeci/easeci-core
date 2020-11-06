package io.easeci.core.workspace.cache;

import io.easeci.commons.DirUtils;

import java.nio.file.Path;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;
import static java.util.Objects.isNull;

public class CacheManager implements CacheGarbageCollector {
    public final static String CACHE_DIRECTORY = "/.cache/";
    private static CacheManager cacheManager;

    private CacheManager() {
        this.initializeDirectory();
    }

    public static CacheManager getInstance() {
        if (isNull(cacheManager)) {
            CacheManager.cacheManager = new CacheManager();
        }
        return cacheManager;
    }

    private Path initializeDirectory() {
        final String workspaceLocation = getWorkspaceLocation();
        final String cacheDirLocation = workspaceLocation.concat(CACHE_DIRECTORY);
        if (!DirUtils.isDirectoryExists(cacheDirLocation)) {
            Path path = DirUtils.directoryCreate(cacheDirLocation);
            logit(WORKSPACE_EVENT, "Directory for cache file just created at here: " + path, THREE);
            return path;
        }
        return Path.of(cacheDirLocation);
    }

    @Override
    public void delayedCleanup(Path path) {

    }
}
