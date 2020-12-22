package io.easeci.core.engine.easefile.parser;

import lombok.Getter;

@Getter
class EasefileParseException extends Exception {
    private EasefileParseResult parseResult;

    public EasefileParseException(EasefileParseResult parseResult) {
        this.parseResult = parseResult;
    }
}
