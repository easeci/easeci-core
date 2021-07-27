package io.easeci.core.engine.easefile.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.engine.EngineStatus;
import io.easeci.core.engine.easefile.parser.analyse.SyntaxError;
import io.easeci.core.engine.easefile.parser.parts.ParsingError;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EasefileParseResult {
    @JsonIgnore
    private boolean success;
    private Path pipelineFilePath;
    private UUID pipelineId;
    private EngineStatus engineStatus;
    private List<SyntaxError> syntaxErrors;
    private List<ParsingError> parsingErrors;

    public static EasefileParseResult success(boolean success, Path pipelineFilePath, UUID pipelineId) {
        EasefileParseResult easefileParseResult = new EasefileParseResult();
        easefileParseResult.success = success;
        easefileParseResult.pipelineFilePath = pipelineFilePath;
        easefileParseResult.pipelineId = pipelineId;
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

    public static EasefileParseResult criticalFailure(EngineStatus engineStatus, List<ParsingError> parsingErrors) {
        EasefileParseResult easefileParseResult = new EasefileParseResult();
        easefileParseResult.success = false;
        easefileParseResult.engineStatus = engineStatus;
        easefileParseResult.parsingErrors = parsingErrors;
        return easefileParseResult;
    }
}
