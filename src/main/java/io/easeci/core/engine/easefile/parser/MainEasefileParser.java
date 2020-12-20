package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.workspace.projects.PipelinePointerIO;

import java.nio.file.Path;

public class MainEasefileParser extends EasefileParserTemplate {

    public MainEasefileParser(PipelinePointerIO pipelinePointerIO) {
        super(pipelinePointerIO);
    }

    byte[] serialize(Pipeline pipeline) {
        return null;
    }

    Path writePipelineFile(byte[] content) {
        return null;
    }

    Pipeline process(String easefileContent) throws StaticAnalyseException {
        return null;
    }
}
