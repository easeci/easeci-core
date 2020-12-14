package io.easeci.core.engine.easefile.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileParseResult {
    private boolean success;
    private Path pipelineFilePath;
    private EngineStatus engineStatus;
    private List<SyntaxError> syntaxErrors;

    public static EasefileParseResult success(boolean success, Path pipelineFilePath) {
        EasefileParseResult easefileParseResult = new EasefileParseResult();
        easefileParseResult.success = success;
        easefileParseResult.pipelineFilePath = pipelineFilePath;
        easefileParseResult.engineStatus = EngineStatus.S_EP_0000;
        return easefileParseResult;
    }

    public static EasefileParseResult failure(EngineStatus engineStatus, List<SyntaxError> syntaxErrors) {
        EasefileParseResult easefileParseResult = new EasefileParseResult();
        easefileParseResult.success = false;
        easefileParseResult.engineStatus = engineStatus;
        easefileParseResult.syntaxErrors = syntaxErrors;
        return easefileParseResult;
    }
}
