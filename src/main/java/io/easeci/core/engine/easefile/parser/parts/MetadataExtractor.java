package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;

public interface MetadataExtractor {

    List<Line> fetchCrudeMetadata() throws PipelinePartError;
}
