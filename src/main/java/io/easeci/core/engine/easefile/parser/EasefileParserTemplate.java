package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.engine.easefile.parser.parts.PipelinePartCriticalError;
import io.easeci.core.engine.pipeline.EasefileObjectModel;
import io.easeci.core.workspace.projects.PipelinePointerIO;

import java.nio.file.Path;
import java.util.Date;

import static io.easeci.core.engine.EngineStatus.F_EP_0002;
import static io.easeci.core.engine.EngineStatus.F_EP_0003;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.EASEFILE_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;

abstract class EasefileParserTemplate implements EasefileParser {

    private PipelinePointerIO pipelinePointerIO;

    public EasefileParserTemplate(PipelinePointerIO pipelinePointerIO) {
        this.pipelinePointerIO = pipelinePointerIO;
    }

    @Override
    public EasefileParseResult parse(String easefileContent, Path easefileSource) {
        try {
            EasefileObjectModel eom = process(easefileContent);
            eom.getMetadata().setEasefilePath(easefileSource);
            return afterParsingSuccess(eom);
        } catch (StaticAnalyseException e) {
            e.printStackTrace();
            return EasefileParseResult.failure(F_EP_0002, e.getSyntaxErrorList());
        } catch (PipelinePartCriticalError e) {
            e.printStackTrace();
            return EasefileParseResult.criticalFailure(F_EP_0003, e.getParsingErrors());
        }
    }

    abstract EasefileObjectModel process(String easefileContent) throws StaticAnalyseException, PipelinePartCriticalError;

    abstract Path createEmptyPipelineFile();

    abstract Path writePipelineFile(Path pipelineFile, EasefileObjectModel easefileObjectModel);

    private EasefileParseResult afterParsingSuccess(EasefileObjectModel eom) {
        final Path pipelineFilePath = createEmptyPipelineFile();
        eom.getMetadata().setPipelineFilePath(pipelineFilePath);
        eom.getMetadata().setCreatedDate(new Date());
        writePipelineFile(pipelineFilePath, eom);
        logit(EASEFILE_EVENT, "Pipeline was serialized, wrote to file and placed here: " + pipelineFilePath.toString());
        pipelinePointerIO.createNewPipelinePointer(eom.getMetadata());
        logit(EASEFILE_EVENT, "Easefile parsed successfully and pipeline pointer added with metadata: " + eom.getMetadata());
        return EasefileParseResult.success(true, eom.getMetadata().getPipelineFilePath());
    }
}
