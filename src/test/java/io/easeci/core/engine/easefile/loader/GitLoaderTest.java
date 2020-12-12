package io.easeci.core.engine.easefile.loader;

import io.easeci.BaseWorkspaceContextTest;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GitLoaderTest extends BaseWorkspaceContextTest {

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
    @Order(6)
    @DisplayName("Should correctly find Easefile in git repository")
    void loadSuccessTest() throws IOException, GitAPIException, IllegalAccessException, EasefileContentMalformed {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO);

        String providedContent = gitLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }

    @Test
    @Order(1)
    @DisplayName("Should correctly find Easefile in git repository from URL with suffix '.git'")
    void loadSuccessSuffixTest() throws IOException, GitAPIException, IllegalAccessException, EasefileContentMalformed {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_WITH_SUFFIX);

        String providedContent = gitLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }

    @Test
    @Order(3)
    @DisplayName("Should throw exception because there is no such repository")
    void failureNotExistsTest() {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_NOT_EXISTS);

        assertThrows(InvalidRemoteException.class, gitLoader::provide);
    }

    @Test
    @Order(4)
    @DisplayName("Should throw exception because there is no Easefile in repository")
    void failureNoEasefileTest() {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO_NO_EASEFILE);

        assertThrows(IllegalStateException.class, gitLoader::provide);
    }
}