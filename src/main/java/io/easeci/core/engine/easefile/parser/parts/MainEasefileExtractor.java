package io.easeci.core.engine.easefile.parser.parts;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

public class MainEasefileExtractor implements EasefileExtractor, MetadataExtractor, KeyExtractor,
                                              VariableExtractor, StageExtractor, ExecutorExtractor {

    private String crudeMetadata;
    private String crudeKey;
    private String crudeExecutor;
    private String crudeVariable;
    private String crudeStage;

    @Override
    public void split(String easefileContent) {
//        todo
    }

    @Override
    public String fetchCrudeMetadata() throws PipelinePartError {
        if (isNull(this.crudeMetadata)) {
            throw new PipelinePartError(error("Metadata"));
        }
        return this.crudeMetadata;
    }

    @Override
    public String fetchCrudeKey() throws PipelinePartError {
        if (isNull(this.crudeKey)) {
            throw new PipelinePartError(error("Key"));
        }
        return this.crudeKey;
    }

    @Override
    public String fetchCrudeVariable() throws PipelinePartError {
        if (isNull(this.crudeVariable)) {
            throw new PipelinePartError(error("Variable"));
        }
        return this.crudeVariable;
    }

    @Override
    public String fetchCrudeStage() throws PipelinePartError {
        if (isNull(this.crudeStage)) {
            throw new PipelinePartError(error("Stage"));
        }
        return this.crudeStage;
    }

    @Override
    public String fetchCrudeExecutor() throws PipelinePartError {
        if (isNull(this.crudeExecutor)) {
            throw new PipelinePartError(error("Executor"));
        }
        return this.crudeExecutor;
    }

    private List<ParsingError> error(String easefilePartName) {
        return Collections.singletonList(
                ParsingError.of(
                        easefilePartName + " Easefile part was not extracted yet",
                        "First of all, use split(...) method, next you will be able to fetch this Easefile part",
                        easefilePartName + " Easefile part was not extracted yet"
                )
        );
    }
}
