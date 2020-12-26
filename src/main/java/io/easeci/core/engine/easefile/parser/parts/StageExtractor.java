package io.easeci.core.engine.easefile.parser.parts;

public interface StageExtractor {

    String fetchCrudeStage() throws PipelinePartError;
}
