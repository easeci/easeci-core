package io.easeci.core.engine.easefile.parser.parts;

import lombok.Getter;

import java.util.List;

@Getter
public class PipelinePartCriticalError extends Exception {
    private List<ParsingError> parsingErrors;

    public PipelinePartCriticalError(List<ParsingError> parsingErrors) {
        this.parsingErrors = parsingErrors;
    }
}
