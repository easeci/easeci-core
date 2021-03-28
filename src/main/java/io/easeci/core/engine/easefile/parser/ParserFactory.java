package io.easeci.core.engine.easefile.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.easeci.core.engine.easefile.parser.parts.*;
import io.easeci.core.workspace.projects.ProjectManager;

public class ParserFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public enum ParserType {
        STANDARD
    }

    public static EasefileParser factorize(ParserType parserType) {
        if (parserType.equals(ParserType.STANDARD)) {
            return MainEasefileParser.builder()
                    .pipelinePointerIO(ProjectManager.getInstance())
                    .easefileExtractor(new MainEasefileExtractor())
                    .metadataProcessor(new MetadataProcessor(objectMapper))
                    .keyProcessor(new KeyProcessor())
                    .executorsProcessor(new ExecutorProcessor(objectMapper))
                    .varsProcessor(new VariableProcessor(objectMapper))
                    .stagesProcessor(new StageProcessor(objectMapper))
                    .scriptFileProcessor(new ScriptFileProcessor())
                    .build();
        }
        throw new IllegalArgumentException("Cannot match any EasefileParser to factorize");
    }
}
