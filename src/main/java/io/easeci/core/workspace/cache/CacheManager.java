package io.easeci.core.workspace.cache;

import io.easeci.commons.DirUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.*;
import static java.util.Objects.isNull;

public class CacheManager implements CacheGarbageCollector, CacheTemp {
    public final static String CACHE_DIRECTORY = "/.cache/";
    public final static String CACHE_TMP_DIRECTORY = "/.cache/temp/";
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
        final String cacheTmpDirLocation = workspaceLocation.concat(CACHE_TMP_DIRECTORY);
        if (!DirUtils.isDirectoryExists(cacheDirLocation)) {
            Path path = DirUtils.directoryCreate(cacheDirLocation);
            DirUtils.directoryCreate(cacheTmpDirLocation);
            logit(WORKSPACE_EVENT, "Directory for cache file just created at here: " + path, THREE);
            return path;
        }
        return Path.of(cacheDirLocation);
    }

    @Override
    public long cleanup(Path concretePath) {
        logit(WORKSPACE_EVENT, "Cleaning up Easeci cache in path: " + concretePath, THREE);
        return clean(concretePath);
    }

    @Override
    public long cleanup() {
        logit(WORKSPACE_EVENT, "Cleaning up entire Easeci cache", THREE);
        Path cacheDirectoryLocation = getCacheDirectoryLocation();
        return clean(cacheDirectoryLocation);
    }

    @Override
    public Path save(byte[] value) {
        final String fileName = "tmp-".concat(String.valueOf(Objects.hashCode(new Date())));
        Path filePath = Path.of(fileName);
        try {
            Files.createFile(filePath);
            Files.write(filePath, value);
        } catch (IOException e) {
            e.printStackTrace();
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                logit(WORKSPACE_EVENT, "Could not created file and save content to this: " + filePath, THREE);
                throw new IllegalStateException("Could not created file and save content to this: " + filePath);
            }
        }
        logit(WORKSPACE_EVENT, "Saved temporary file here: " + filePath, THREE);
        return filePath;
    }

    @Override
    public Path save(String value) {
        return save(value.getBytes());
    }

    private long clean(Path cacheDirectoryLocation) {
        if (!hasAccessRight(cacheDirectoryLocation)) {
            logit(WORKSPACE_EVENT, "Access denied for trying to removing cache from path: " + cacheDirectoryLocation, THREE);
            return 0;
        }
        try {
            long totalByteSize = Files.list(cacheDirectoryLocation)
                    .map(this::removeResource)
                    .reduce(Long::sum)
                    .orElse(0L);
            logit(WORKSPACE_EVENT, totalByteSize + " bytes of cache resource freed", THREE);
            return totalByteSize;
        } catch (IOException e) {
            e.printStackTrace();
            logit(WORKSPACE_EVENT, 0 + " bytes of cache resource freed. Removing cache ends with exception", THREE);
            return 0;
        }
    }

    private long removeResource(Path resource) {
        long byteSize = byteSize(resource);
        if (Files.isDirectory(resource)) {
            try {
                FileUtils.deleteDirectory(resource.toFile());
                return byteSize;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            try {
                FileUtils.forceDelete(resource.toFile());
                return byteSize;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    private boolean hasAccessRight(Path path) {
        String cacheLocation = String.valueOf(getCacheDirectoryLocation());
        return path.toString().startsWith(cacheLocation) || path.toString().equals(cacheLocation);
    }

    private long byteSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
