package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.workspace.projects.PipelinePointer;
import io.easeci.core.workspace.projects.PipelinePointerIO;
import io.easeci.core.workspace.projects.ProjectManager;

import java.nio.file.Path;

public class MainEasefileParser implements EasefileParser {
    private PipelinePointerIO pipelinePointerIO;

    public MainEasefileParser() {
        this.pipelinePointerIO = ProjectManager.getInstance();
    }

    @Override
    public EasefileParseResult parse(String easefileContent) {

        try {
            // 1. prepare pipeline
            Pipeline pipeline = process(easefileContent);
            // 2. make pipelinePointer, embed pipeline in project-structure.json
            PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(pipeline.getMetadata());
            // 3. return success response
            return EasefileParseResult.success(true, pipeline.getMetadata().getPipelineFilePath());
        } catch (StaticAnalyseException e) {
            // 4. end with parse errors, indicates errors very precisely
            return EasefileParseResult.failure(null, e.getSyntaxErrorList());
        }
    }

    private byte[] serialize(Pipeline pipeline) {
        return null;
    }

    private Path writePipelineFile(byte[] content) {
        return null;
    }

    private Pipeline process(String easefileContent) throws StaticAnalyseException {
        return null;
    }
}
