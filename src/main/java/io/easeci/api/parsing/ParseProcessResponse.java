package io.easeci.api.parsing;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.easeci.core.engine.easefile.parser.EasefileParseResult;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParseProcessResponse {
    private Boolean isSuccessfullyDone;
    private String message;
    private EasefileParseResult easefileParseResult;

    public static ParseProcessResponse of(EasefileParseResult easefileParseResult) {
        ParseProcessResponse response = new ParseProcessResponse();
        response.setIsSuccessfullyDone(easefileParseResult.isSuccess());
        response.setEasefileParseResult(easefileParseResult);
        return response;
    }

    public static ParseProcessResponse withError(String exceptionMessage) {
        ParseProcessResponse response = new ParseProcessResponse();
        response.isSuccessfullyDone = false;
        response.message = exceptionMessage;
        return response;
    }
}
