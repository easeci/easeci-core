package io.easeci.core.workspace.easefiles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class EasefileManagerTest {

    @Test
    @DisplayName("Should return path one directory backward")
    void backwardPathTest() {
        Path input = Paths.get("/var/html/website");

        Path output = EasefileManager.pathBackward(input);

        assertEquals("/var/html", output.toString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passed null as method argument")
    void backwardPathNullTest() {
        Path input = null;

        assertThrows(IllegalArgumentException.class, () -> EasefileManager.pathBackward(input));
    }

    @Test
    @DisplayName("Should return only root directory '/' when there is only one path after slash")
    void backwardPathNotBackwardsPath() {
        Path input = Paths.get("/home");

        Path output = EasefileManager.pathBackward(input);

        assertEquals("/", output.toString());
    }
}