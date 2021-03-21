package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.parts.Line;
import io.easeci.core.engine.pipeline.ExecutorConfiguration;
import io.easeci.core.engine.pipeline.Key;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static commons.WorkspaceTestUtils.buildPathFromResources;

public class Utils {

    static EasefileObjectModel provideEmptyPipelineForTest() {
        return EasefileObjectModel.builder()
                .metadata(new EasefileObjectModel.Metadata())
                .key(Key.of(Key.KeyType.PIPELINE))
                .executorConfiguration(new ExecutorConfiguration())
                .variables(Collections.singletonList(Variable.of(VariableType.STRING, "title", "value")))
                .stages(Collections.singletonList(Stage.builder()
                        .build()))
                .scriptEncoded(new byte[0])
                .build();
    }

    public static String readEmptyExecutorTestEasefile() {
        return load("workspace/Easefile_empty_executor");
    }

    public static String readEmptyMetadataTestEasefile() {
        return load("workspace/Easefile_empty_metadata");
    }

    public static String readEasefileAsYaml() {
        return load("workspace/Easefile_yaml");
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
