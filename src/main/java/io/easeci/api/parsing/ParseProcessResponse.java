package io.easeci.api.parsing;

import io.easeci.core.engine.easefile.parser.EasefileParseResult;
import lombok.Data;

@Data
public class ParseProcessResponse {
    private Boolean isSuccessfullyDone;
    private String message;

    public static ParseProcessResponse of(EasefileParseResult easefileParseResult) {
        return null;
    }

    public static ParseProcessResponse withError(String exceptionMessage) {
        ParseProcessResponse response = new ParseProcessResponse();
        response.isSuccessfullyDone = false;
        response.message = exceptionMessage;
        return response;
    }
}
