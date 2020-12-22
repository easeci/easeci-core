package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.parts.*;

public class ParserFactory {

    public enum ParserType {
        STANDARD
    }

    public static EasefileParser factorize(ParserType parserType) {
        if (parserType.equals(ParserType.STANDARD)) {
            return MainEasefileParser.builder()
                    .metadataProcessor(new MetadataProcessor())
                    .keyProcessor(new KeyProcessor())
                    .executorsProcessor(new ExecutorProcessor())
                    .varsProcessor(new VariableProcessor())
                    .stagesProcessor(new StageProcessor())
                    .scriptFileProcessor(new ScriptFileProcessor())
                    .build();
        }
        throw new IllegalArgumentException("Cannot match any EasefileParser to factorize");
    }
}
