package io.easeci.core.engine.easefile.parser.parts;

public interface EasefileExtractor {

    void split(String easefileContent) throws PipelinePartCriticalError;
}
