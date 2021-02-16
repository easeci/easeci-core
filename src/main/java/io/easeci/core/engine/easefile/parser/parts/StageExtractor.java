package io.easeci.core.engine.easefile.parser.parts;

import java.util.List;

public interface StageExtractor {

    List<Line> fetchCrudeStage() throws PipelinePartError;
}
