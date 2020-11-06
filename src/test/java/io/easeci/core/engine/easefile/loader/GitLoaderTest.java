package io.easeci.core.engine.easefile.loader;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static io.easeci.core.workspace.LocationUtils.getCacheDirectoryLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitLoaderTest {

    // repository with Easefile placed in
    final static String GITHUB_PUBLIC_REPO = "https://github.com/meksula/notebook";
    final static String GITHUB_PUBLIC_REPO_WITH_SUFFIX = "https://github.com/meksula/notebook";
    final static String GITHUB_PUBLIC_REPO_NO_EASEFILE = "https://github.com/meksula/programming-challenges.git";
    final static String GITHUB_PUBLIC_REPO_NOT_EXISTS = "https://github.com/meksula";

    final static String EASEFILE_FROM_GITHUB = "pipeline:\n" +
            "  executor: auto\n" +
            "  stage 'Print Hello World':\n" +
            "     $bash '''\n" +
            "         echo 'Hello World from EaseCI'\n" +
            "     '''\n";

    @Test
    @DisplayName("Should correctly find Easefile in git repository")
    void loadSuccessTest() throws IOException, GitAPIException, IllegalAccessException, EasefileContentMalformed {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO);

        String providedContent = gitLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }

    @Test
    @DisplayName("Should correctly find Easefile in git repository from URL with suffix '.git'")
    void loadSuccessSuffixTest() throws IOException, GitAPIException, IllegalAccessException, EasefileContentMalformed {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_WITH_SUFFIX);

        String providedContent = gitLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }

    @Test
    @DisplayName("Should throw exception because there is no such repository")
    void failureNotExistsTest() {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_NOT_EXISTS);

        assertThrows(InvalidRemoteException.class, gitLoader::provide);
    }

    @Test
    @DisplayName("Should throw exception because there is no Easefile in repository")
    void failureNoEasefileTest() {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_NO_EASEFILE);

        assertThrows(IllegalStateException.class, gitLoader::provide);
    }

    @AfterEach
    void cleanup() throws IOException {
        Path cacheDirectoryLocation = getCacheDirectoryLocation();
        FileUtils.deleteDirectory(cacheDirectoryLocation.toFile());
    }
}