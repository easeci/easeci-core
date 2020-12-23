package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.pipeline.Key;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.engine.pipeline.Stage;
import io.easeci.core.workspace.vars.Variable;
import io.easeci.extension.command.VariableType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static commons.WorkspaceTestUtils.buildPathFromResources;

public class Utils {

    static Pipeline provideEmptyPipelineForTest() {
        return Pipeline.builder()
                .metadata(new Pipeline.Metadata())
                .key(Key.of(Key.KeyType.PIPELINE))
                .executors(Collections.emptyList())
                .variables(Collections.singletonList(Variable.of(VariableType.STRING, "title", "value")))
                .stages(Collections.singletonList(Stage.builder()
                        .build()))
                .scriptEncoded(new byte[0])
                .build();
    }

    static String readValidTestEasefile() {
        Path path = buildPathFromResources("workspace/Easefile");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
