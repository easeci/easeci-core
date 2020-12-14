package io.easeci.core.engine.easefile.parser.analyse;

import io.easeci.core.engine.EngineStatus;

import java.util.List;

public class StaticAnalyseException extends Exception {
    private final EngineStatus engineStatus;
    private final List<SyntaxError> syntaxErrorList;

    public StaticAnalyseException(EngineStatus engineStatus, List<SyntaxError> syntaxErrorList) {
        this.engineStatus = engineStatus;
        this.syntaxErrorList = syntaxErrorList;
    }

    public List<SyntaxError> getSyntaxErrorList() {
        return syntaxErrorList;
    }
}
