package io.easeci.core.engine.easefile.loader;

import io.easeci.core.workspace.LocationUtils;
import io.easeci.core.workspace.cache.CacheGarbageCollector;
import io.easeci.core.workspace.cache.CacheManager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.EASEFILE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.THREE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;

public class GitLoader implements EasefileLoader {
    private String gitRepositoryUrl;

    public static EasefileLoader of(String gitRepositoryUrl) {
        GitLoader easefileLoader = new GitLoader();
        easefileLoader.gitRepositoryUrl = gitRepositoryUrl;
        return easefileLoader;
    }

    @Override
    public String provide() throws GitAPIException, IOException {
        final File repositoryDestination = repositoryFilesDestination();

        if (Files.exists(repositoryDestination.toPath())) {
            final Path easefile = findEasefile(repositoryDestination.toPath());
            logit(EASEFILE_EVENT, "Loading content to parsing Easefile from git repository from remote: "
                    + gitRepositoryUrl + ". That just exists in local workspace", THREE);
            return readFile(easefile.toFile());
        }

        Git.cloneRepository()
                .setURI(gitRepositoryUrl)
                .setDirectory(repositoryDestination)
                .call();

        final Path easefile = findEasefile(repositoryDestination.toPath());
        final String easefileContent = readFile(easefile.toFile());

        CacheGarbageCollector cacheGarbageCollector = CacheManager.getInstance();
        cacheGarbageCollector.cleanup(repositoryDestination.toPath());
        logit(EASEFILE_EVENT, "Loading content to parsing Easefile from git repository from remote: " + gitRepositoryUrl, THREE);
        return easefileContent;
    }

    private String readFile(File easefile) throws IOException {
        return FileUtils.readFileToString(easefile, "UTF-8");
    }

    private File repositoryFilesDestination() {
        return Paths.get(LocationUtils.getCacheDirectoryLocation().toString()
                .concat("/")
                .concat(repositoryName()))
                .toFile();
    }

    private String repositoryName() {
        String[] parts = gitRepositoryUrl.split("/");
        return parts[parts.length - 1].trim();
    }

    private Path findEasefile(Path repositoryLocalPath) throws IOException {
        final String EASEFILE_NAME_PATTERN = ".+/[Ee]asefile[./\\s^]*?";
        return Files.list(repositoryLocalPath)
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> path.toString().matches(EASEFILE_NAME_PATTERN))
                .findFirst()
                .orElseThrow(() -> {
                    CacheGarbageCollector cacheGarbageCollector = CacheManager.getInstance();
                    cacheGarbageCollector.cleanup(repositoryLocalPath);
                    throw new IllegalStateException("Easefile not exists in repository");
                });
    }
}
