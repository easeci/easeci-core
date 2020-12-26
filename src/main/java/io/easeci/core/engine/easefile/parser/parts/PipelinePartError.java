package io.easeci.core.engine.easefile.parser.parts;

import lombok.Getter;

import java.util.List;

@Getter
public class PipelinePartError extends RuntimeException {
    private List<ParsingError> parsingErrors;

    public PipelinePartError(List<ParsingError> parsingErrors) {
        this.parsingErrors = parsingErrors;
    }
}
