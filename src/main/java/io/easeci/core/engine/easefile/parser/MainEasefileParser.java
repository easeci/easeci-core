package io.easeci.core.engine.easefile.parser;

import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.StaticAnalyseException;
import io.easeci.core.workspace.projects.PipelinePointer;
import io.easeci.core.workspace.projects.PipelinePointerIO;
import io.easeci.core.workspace.projects.ProjectManager;

public class MainEasefileParser implements EasefileParser {
    private PipelinePointerIO pipelinePointerIO;

    public MainEasefileParser() {
        this.pipelinePointerIO = ProjectManager.getInstance();
    }

    @Override
    public EasefileParseResult parse(String easefileContent) throws StaticAnalyseException {

//        TODO

        System.out.println(easefileContent);

        EasefileParseResult easefileParseResult = new EasefileParseResult();

        PipelinePointer pipelinePointer = pipelinePointerIO.createNewPipelinePointer(easefileParseResult.getPipeline().getMetadata());

        if (pipelinePointer != null) {
            EngineStatus ePp0001 = EngineStatus.F_PP_0001;
            ePp0001.className = this.getClass().getName();
            ePp0001.codeLine = Thread.currentThread().getStackTrace()[0].getLineNumber();
            easefileParseResult.putError(ePp0001);
        }

        return easefileParseResult;
    }
}
