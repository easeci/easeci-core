package io.easeci.core.engine.easefile.parser.parts;

public interface KeyExtractor {

    String fetchCrudeKey() throws PipelinePartError;
}
