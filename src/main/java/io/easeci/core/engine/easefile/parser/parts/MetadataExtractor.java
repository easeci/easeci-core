package io.easeci.core.engine.easefile.parser.parts;

public interface MetadataExtractor {

    String fetchCrudeMetadata() throws PipelinePartError;
}
