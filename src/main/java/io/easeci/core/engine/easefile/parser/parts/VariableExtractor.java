package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;

public interface VariableExtractor {

    List<Line> fetchCrudeVariable() throws PipelinePartError;
}
