package io.easeci.core.engine.easefile.loader;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitLoaderTest {

    // repository with Easefile placed in
    final static String GITHUB_PUBLIC_REPO = "https://github.com/meksula/notebook";

    final static String EASEFILE_FROM_GITHUB = "pipeline:\n" +
            "  executor: auto\n" +
            "  stage 'Print Hello World':\n" +
            "     $bash '''\n" +
            "         echo 'Hello World from EaseCI'\n" +
            "     '''\n";

    @Test
    @DisplayName("Should correctly find Easefile in git repository")
    void loadSuccessTest() throws IOException, GitAPIException, IllegalAccessException {
        EasefileLoader gitLoader = GitLoader.of(GITHUB_PUBLIC_REPO);

        String providedContent = gitLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }
}