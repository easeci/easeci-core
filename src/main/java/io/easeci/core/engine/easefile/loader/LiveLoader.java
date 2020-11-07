package io.easeci.core.engine.easefile.loader;

import io.easeci.commons.DirUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.EASEFILE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.WORKSPACE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static io.easeci.core.workspace.LocationUtils.getCacheDirectoryLocation;
import static io.easeci.core.workspace.LocationUtils.getWorkspaceLocation;
import static io.easeci.core.workspace.cache.CacheManager.CACHE_DIRECTORY;
import static java.util.Objects.nonNull;

public class LiveLoader implements EasefileLoader, Serializable {
    private static final String LIVE_CACHED_FILES = "/easefiles-live/";
    private Path localStoragePath;
    private String encodedEasefileContent;

    public static LiveLoader of(String localStoragePath, String encodedEasefileContent) {
        LiveLoader liveLoader = new LiveLoader();
        if (nonNull(localStoragePath)) {
            liveLoader.localStoragePath = Path.of(localStoragePath);
        }
        liveLoader.encodedEasefileContent = encodedEasefileContent;
        initializeDirectory();
        return liveLoader;
    }

    public static LiveLoader of(String encodedEasefileContent) {
        LiveLoader liveLoader = new LiveLoader();
        liveLoader.encodedEasefileContent = encodedEasefileContent;
        initializeDirectory();
        return liveLoader;
    }

    private static Path initializeDirectory() {
        final String workspaceLocation = getWorkspaceLocation();
        final String cacheDirLiveFilesLocation = workspaceLocation.concat(CACHE_DIRECTORY + LIVE_CACHED_FILES);
        if (!DirUtils.isDirectoryExists(cacheDirLiveFilesLocation)) {
            Path path = DirUtils.directoryCreate(cacheDirLiveFilesLocation);
            logit(WORKSPACE_EVENT, "Directory for caching easefiles from live analyse just created at here: " + path, THREE);
            return path;
        }
        return Path.of(cacheDirLiveFilesLocation);
    }

    @Override
    public String provide() throws IOException, EasefileContentMalformed {
        final String tmpEasefileName = getCacheDirectoryLocation()
                                            .toString()
                                            .concat(LIVE_CACHED_FILES).concat("Easefile")
                                            .concat(String.valueOf(new Date().hashCode()));
        try {
            byte[] decode = Base64.getDecoder().decode(encodedEasefileContent);
            String decodedContent = new String(decode, StandardCharsets.UTF_8);
            Path createdFile = Files.createFile(Paths.get(tmpEasefileName));
            Files.writeString(createdFile, decodedContent);
            if (nonNull(this.localStoragePath)) {
                Files.writeString(this.localStoragePath, decodedContent);
            }
            logit(EASEFILE_EVENT, "Loading content to parsing Easefile live from request", THREE);
            return decodedContent;
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
            final String errorMessage = "Content of Easefile to parse is malformed. Maybe not Base64 encoded?";
            logit(EASEFILE_EVENT, errorMessage, THREE);
            throw new EasefileContentMalformed(errorMessage);
        }
    }
}
