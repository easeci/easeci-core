package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.parts.Line;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static commons.WorkspaceTestUtils.buildPathFromResources;

public class Utils {

    public static String readEmptyExecutorTestEasefile() {
        return load("workspace/Easefile_empty_executor");
    }

    public static String readEmptyMetadataTestEasefile() {
        return load("workspace/Easefile_empty_metadata");
    }

    public static String readFinalCorrectEasefile() {
        return load("workspace/Easefile_sample");
    }

    public static List<Line> wrapLines(String easefilePart) {
        String[] split = easefilePart.split("\n");
        int lineNumber = 1;
        List<Line> lines = new ArrayList<>();
        for (String line : split) {
            lines.add(Line.of(lineNumber, line));
        }
        return lines;
    }

    private static String load(String stringPath) {
        Path path = buildPathFromResources(stringPath);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
