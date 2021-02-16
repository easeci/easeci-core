package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;

public interface KeyExtractor {

    List<Line> fetchCrudeKey() throws PipelinePartError;
}
