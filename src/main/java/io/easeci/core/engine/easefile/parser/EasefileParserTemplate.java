package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.pipeline.Pipeline;
import io.easeci.core.workspace.projects.PipelinePointerIO;

import java.nio.file.Path;

import static io.easeci.core.engine.EngineStatus.F_EP_0002;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.EASEFILE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;

public abstract class EasefileParserTemplate implements EasefileParser {

    private PipelinePointerIO pipelinePointerIO;

    public EasefileParserTemplate(PipelinePointerIO pipelinePointerIO) {
        this.pipelinePointerIO = pipelinePointerIO;
    }

    @Override
    public EasefileParseResult parse(String easefileContent) {
        try {
            Pipeline pipeline = process(easefileContent);
            return afterParsingSuccess(pipeline);
        } catch (StaticAnalyseException e) {
            return EasefileParseResult.failure(F_EP_0002, e.getSyntaxErrorList());
        }
    }

    abstract Pipeline process(String easefileContent) throws StaticAnalyseException;

    abstract byte[] serialize(Pipeline pipeline);

    abstract Path writePipelineFile(byte[] content);

    private EasefileParseResult afterParsingSuccess(Pipeline pipeline) {
        final byte[] serializedPipeline = serialize(pipeline);
        final Path pipelineFilePath = writePipelineFile(serializedPipeline);
        logit(EASEFILE_EVENT, "Pipeline was serialized, wrote to file and placed here: " + pipelineFilePath.toString());
        pipelinePointerIO.createNewPipelinePointer(pipeline.getMetadata());
        logit(EASEFILE_EVENT, "Easefile parsed successfully and pipeline pointer added with metadata: " + pipeline.getMetadata());
        return EasefileParseResult.success(true, pipeline.getMetadata().getPipelineFilePath());
    }
}
