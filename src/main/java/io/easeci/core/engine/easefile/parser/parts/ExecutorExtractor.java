package io.easeci.core.engine.easefile.parser.parts;

public interface ExecutorExtractor {

    String fetchCrudeExecutor() throws PipelinePartError;
}
