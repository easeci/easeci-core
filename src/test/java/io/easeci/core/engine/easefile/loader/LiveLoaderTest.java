package io.easeci.core.engine.easefile.loader;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.easeci.core.engine.easefile.loader.GitLoaderTest.EASEFILE_FROM_GITHUB;
import static org.junit.jupiter.api.Assertions.*;

class LiveLoaderTest {

    @Test
    @DisplayName("Should correctly load content from request and save backup in cache")
    void successTest() throws IllegalAccessException, GitAPIException, IOException, EasefileContentMalformed {
        byte[] encoded = Base64.getEncoder().encode(EASEFILE_FROM_GITHUB.getBytes());
        final String encodedEasefile = new String(encoded, StandardCharsets.UTF_8);

        EasefileLoader easefileLoader = LiveLoader.of(encodedEasefile);

        String providedContent = easefileLoader.provide();

        assertEquals(EASEFILE_FROM_GITHUB, providedContent);
    }

    @Test
    @DisplayName("Should throw when trying to parse not encoded content")
    void failureEncodingTest() {
        EasefileLoader easefileLoader = LiveLoader.of(EASEFILE_FROM_GITHUB);

        assertThrows(EasefileContentMalformed.class, easefileLoader::provide);
    }
}