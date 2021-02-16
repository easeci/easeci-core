package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;

public interface ExecutorExtractor {

    List<Line> fetchCrudeExecutor() throws PipelinePartError;
}
