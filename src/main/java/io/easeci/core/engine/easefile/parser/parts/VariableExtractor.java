package io.easeci.core.engine.easefile.parser.parts;

public interface VariableExtractor {

    String fetchCrudeVariable() throws PipelinePartError;
}
